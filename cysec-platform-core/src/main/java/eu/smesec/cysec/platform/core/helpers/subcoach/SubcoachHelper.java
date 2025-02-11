/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.helpers.subcoach;

import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.FQCN;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.QuestionType;
import eu.smesec.cysec.platform.bridge.generated.SubcoachInstances;
import eu.smesec.cysec.platform.bridge.utils.Tuple;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import org.glassfish.jersey.logging.LoggingFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The subcoach helper is a helper class to do different stuff related to subcoaches instantiated through a subcoach
 * instantiator.
 */
public class SubcoachHelper {

    private final static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    private final String companyId;
    private final FQCN fqcn;
    private final CacheAbstractionLayer cal;

    /**
     * This is a private constructor. Users of this class should always use the factory method.
     */
    private SubcoachHelper(String companyId, FQCN fqcn, CacheAbstractionLayer cal) {
        this.companyId = companyId;
        this.fqcn = fqcn;
        this.cal = cal;
    }

    /**
     * This method returns the first FQCN of a subcoach instantiator. For example if a subcoach instantiator contains
     * subcoach instances A, B and C of the subcoach with ID parentCoach.subCoach this method will return the FQCN
     * parentCoach.subCoach.A
     * @param instantiatorId The ID of the instantiator to get first FQCN of
     * @return Optional containing the first FQCN of the passed instantiator
     * @throws CacheException if access to CAL fails
     */
    public Optional<FQCN> getFirstFqcn(String instantiatorId) throws CacheException {
        return InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, instantiatorId)
                .filter(data -> !data.getInstances().isEmpty())
                .map(data -> FQCN.from(fqcn.getRootCoachId(), data.subcoachId, data.instances.get(0).getInstanceName()));
    }

    /**
     * This method returns the ID of the subcoach instantiator that this subcoach belongs to / was created by.
     * @return Optional containing the ID of the instantiator ID
     * @throws CacheException if something goes wrong accessing the CAL
     */
    public Optional<String> getSubcoachInstantiatorId() throws CacheException {
        Metadata subcoachData = cal.getMetadataOnAnswer(companyId, fqcn, "subcoach-data");
        if (subcoachData != null) {
            return subcoachData.getMvalue().stream()
                    .filter(val -> val.getKey().equals("subcoach-instantiator-id"))
                    .findFirst()
                    .map(val -> val.getStringValueOrBinaryValue().getValue());
        }
        return Optional.empty();
    }

    /**
     * This method returns the question ID of the first outlet that belongs to the instantiator of this instantiator.
     * @return Optional containing the question ID of the first outlet that belongs to the instantiator of this instantiator
     * @throws CacheException if something fails accessing CAL
     */
    public Optional<String> getFirstOutletQuestionId() throws CacheException {
        Optional<String> instantiatorId = getSubcoachInstantiatorId();
        if (instantiatorId.isPresent()) {
            CoachLibrary lib = cal.getLibrariesForQuestionnaire(fqcn.getParentCoachId()).get(0);
            return lib.getQuestionnaire()
                    .getQuestions()
                    .getQuestion()
                    .stream()
                    .filter(q -> q.getType().equals(QuestionType.SUBCOACH_INSTANTIATOR_OUTLET))
                    .filter(q -> q.getSubcoachInstantiatorId().equals(instantiatorId.get()))
                    .map(Question::getId)
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Returns the next subcoach instance in the instantiator relative to the one that is currently active. This can
     * be used to know where to navigate if one subcoach instance is completed and the user clicks "next".
     * @return An optional containing the FQCN of the next subcoach instance relative to the currently active one
     * @throws CacheException if something goes awry when accessing CAL
     */
    public Optional<FQCN> getNextSubcoachInstance() throws CacheException {
        Optional<String> instantiatorId = getSubcoachInstantiatorId();
        Optional<String> currentInstanceName = getCurrentSubcoachInstanceName();
        if (instantiatorId.isPresent() && currentInstanceName.isPresent()) {
            Optional<InstantiatorData> data = InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, instantiatorId.get());
            if (!data.isPresent()) return Optional.empty();

            // First we find the index of the currently active instance
            int currentIndex = 0;
            for (int i = 0; i < data.get().instances.size(); i++) {
                if (data.get().instances.get(i).getInstanceName().equals(currentInstanceName.get())) break;
                currentIndex++;
            }

            // Calculate index of next subcoach
            int nextIndex = currentIndex + 1;

            // Return fqcn of next subcoach if there is one
            if (data.get().instances.size() > nextIndex) {
                FQCN nextFqcn = FQCN.from(fqcn.getRootCoachId(), fqcn.getCoachId(), data.get().instances.get(nextIndex).getInstanceName());
                return Optional.of(nextFqcn);
            }
        }

        return Optional.empty();
    }

    /**
     * This method returns the currently active subcoach instance of the current subcaoch instantiator
     * @return Optional containing the current subcoach instance name
     * @throws CacheException if something goes wrong when accessing CAL
     */
    public Optional<String> getCurrentSubcoachInstanceName() throws CacheException {
        Optional<String> subcoachInstantiatorId = getSubcoachInstantiatorId();
        if (subcoachInstantiatorId.isPresent()) {
            Answer answer = cal.getAnswer(companyId, fqcn.getRoot(), subcoachInstantiatorId.get());
            return Optional.ofNullable(answer.getCurrentSubcoachInstance());
        }
        return Optional.empty();
    }

    /**
     * Gets the currently active subcoach instance of the current subcoach instantiator
     * @return Optional containing the current subcoach instance
     * @throws CacheException if something goes wrong when accessing CAL
     */
    public Optional<SubcoachInstances.SubcoachInstance> getCurrentSubcoachInstance() throws CacheException {
        Optional<String> currentSubcoachInstanceName = getCurrentSubcoachInstanceName();
        if (currentSubcoachInstanceName.isPresent()) {
            Optional<String> instantiatorId = getSubcoachInstantiatorId();
            if (instantiatorId.isPresent()) {
                SubcoachHelper parentHelper = SubcoachHelper.of(companyId, fqcn.getParent(), cal);
                return parentHelper.getAllInstantiatorsInCoach().stream()
                        .filter(data -> data.getInstantiatorId().equals(instantiatorId.get()))
                        .findFirst()
                        .flatMap(data -> data.getInstances().stream()
                                .filter(instance -> instance.getInstanceName().equals(currentSubcoachInstanceName.get()))
                                .findFirst());
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    /**
     * Finds all instantiators in the current coach. Can be used to get a complete list of all subcoaches in the coach.
     * @return List of instantiator data
     * @throws CacheException if something goes wrong accessing CAL
     */
    public List<InstantiatorData> getAllInstantiatorsInCoach() throws CacheException {
        // First we find all subcoach instantiators in this coach
        CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        List<String> instantiatorIds = library.getQuestionnaire()
                .getQuestions()
                .getQuestion()
                .stream()
                .filter(q -> q.getType().equals(QuestionType.SUBCOACH_INSTANTIATOR))
                .map(Question::getId)
                .collect(Collectors.toList());

        // Get data of instantiators and return it
        List<InstantiatorData> instantiators = new ArrayList<>();
        for (String id : instantiatorIds)
            InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, id).ifPresent(instantiators::add);
        return instantiators;
    }

    // Must be called with fqcn = parentFqcn

    /**
     * Given the list of questions in the parent coach this method inserts all subcoach questions at the right position
     * in the list. Important: This method must be called on an instance of SubcoachHelper where fqcn is a fqcn of the
     * parent coach.
     * @param questions The list of questions in the parent coach containing subcoachInstantiatorOutlets
     * @return A list of tuples of FQCNs and Questions. This list contains the inserted questions and every question
     * has it's FQCN attached. This is important because the same question can appear in multiple subcoaches.
     */
    public List<Tuple<FQCN, Question>> insertSubcoachQuestions(List<Question> questions) {
        return questions.stream().flatMap(question -> {
            Tuple<FQCN, Question> questionTuple = new Tuple<>(fqcn, question);
            try {
                // If the question is a subcoach outlet we insert all questions of the subcoach at the position of the outlet
                if (question.getType().equals(QuestionType.SUBCOACH_INSTANTIATOR_OUTLET)) {
                    Optional<InstantiatorData> instantiator = InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, question.getSubcoachInstantiatorId());
                    if (!instantiator.isPresent())
                        return Stream.of(questionTuple);

                    // Find all questions of the subcoach outlet
                    List<Tuple<FQCN, Question>> subcoachQuestions = new ArrayList<>();
                    for (SubcoachInstances.SubcoachInstance instance : instantiator.get().getInstances()) {
                        FQCN subcoachFqcn = FQCN.from(fqcn.getRootCoachId(), instantiator.get().subcoachId, instance.getInstanceName());
                        CoachLibrary subcoachLibrary = cal.getLibrariesForQuestionnaire(subcoachFqcn.getCoachId()).get(0);

                        List<String> activeQuestionIds = subcoachLibrary.getActiveQuestions(subcoachFqcn);
                        for (String qid : activeQuestionIds)
                            subcoachQuestions.add(new Tuple<>(subcoachFqcn, cal.getQuestion(subcoachFqcn.getCoachId(), qid)));
                    }

                    return Stream.concat(Stream.of(questionTuple), subcoachQuestions.stream());
                } else {
                    return Stream.of(questionTuple);
                }
            } catch (CacheException e) {
                logger.severe("An error occurred while getting active questions of subcoaches");
                return Stream.of(questionTuple);
            }
        }).collect(Collectors.toList());
    }

    /**
     * Factory method for SubcoachHelper
     * @param companyId The company ID to operator on
     * @param fqcn The FQCN of the coach
     * @param cal The CAL object to use
     * @return A new SubcoachHelper object
     */
    public static SubcoachHelper of(String companyId, FQCN fqcn, CacheAbstractionLayer cal) {
        return new SubcoachHelper(companyId, fqcn, cal);
    }

    /**
     * Data structure to represent an instantiator
     */
    public final static class InstantiatorData {
        private final String instantiatorId;
        private final String subcoachId;
        private final List<SubcoachInstances.SubcoachInstance> instances;

        private InstantiatorData(String instantiatorId, String subcoachId, List<SubcoachInstances.SubcoachInstance> instances) {
            this.instantiatorId = instantiatorId;
            this.subcoachId = subcoachId;
            this.instances = instances;
        }

        /**
         * Factory method for this class. Returns an instantiator data based on the instantiatorId
         */
        public static Optional<InstantiatorData> ofInstantiatorId(String companyId, FQCN fqcn, CacheAbstractionLayer cal, String instantiatorId) throws CacheException {
            Answer answer = cal.getAnswer(companyId, fqcn.getRoot(), instantiatorId);
            if (answer == null) return Optional.empty();
            Question instantiatorQuestion = cal.getQuestion(fqcn.getRootCoachId(), instantiatorId);
            List<SubcoachInstances.SubcoachInstance> instances = answer.getSubcoachInstances() == null
                    ? new ArrayList<>()
                    : answer.getSubcoachInstances().getSubcoachInstance();
            return Optional.of(new InstantiatorData(instantiatorId, instantiatorQuestion.getSubcoachId(), instances));
        }

        // Getters

        public String getInstantiatorId() {
            return instantiatorId;
        }

        public String getSubcoachId() {
            return subcoachId;
        }

        public List<SubcoachInstances.SubcoachInstance> getInstances() {
            return instances;
        }
    }
}
