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

public class SubcoachHelper {

    private final static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    public static FQCN getFirstFqcn(String companyId, FQCN fqcn, CacheAbstractionLayer cal, String instantiatorId) throws CacheException {
        InstantiatorData data = InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, instantiatorId);
        if (data.instances.isEmpty()) {
            throw new IllegalStateException("Cannot get first instance if no subcoaches are instantiated");
        }

        return FQCN.from(fqcn.getRootCoachId(), data.subcoachId, data.instances.get(0).getSubcoachId());
    }

    public static Optional<String> getSubcoachInstantiatorId(String companyId, FQCN fqcn, CacheAbstractionLayer cal) throws CacheException {
        Metadata subcoachData = cal.getMetadataOnAnswer(companyId, fqcn, "subcoach-data");
        if (subcoachData != null) {
            return subcoachData.getMvalue().stream()
                    .filter(val -> val.getKey().equals("subcoach-instantiator-id"))
                    .findFirst()
                    .map(val -> val.getStringValueOrBinaryValue().getValue());
        }
        return Optional.empty();
    }

    public static Optional<FQCN> getNextSubcoachInstance(String companyId, FQCN fqcn, CacheAbstractionLayer cal) throws CacheException {
        Optional<String> instantiatorId = getSubcoachInstantiatorId(companyId, fqcn, cal);
        Optional<String> currentInstanceName = getCurrentSubcoachInstance(companyId, fqcn, cal);
        if (instantiatorId.isPresent() && currentInstanceName.isPresent()) {
            InstantiatorData data = InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, instantiatorId.get());

            int currentIndex = 0;
            for (int i = 0; i < data.instances.size(); i++) {
                if (data.instances.get(i).getSubcoachId().equals(currentInstanceName.get())) break;
                currentIndex++;
            }

            int nextIndex = currentIndex + 1;

            // Return fqcn of next subcoach if there is one
            if (data.instances.size() > nextIndex) {
                FQCN nextFqcn = FQCN.from(fqcn.getRootCoachId(), fqcn.getCoachId(), data.instances.get(nextIndex).getSubcoachId());
                return Optional.of(nextFqcn);
            }
        }

        return Optional.empty();
    }

    public static Optional<String> getCurrentSubcoachInstance(String companyId, FQCN fqcn, CacheAbstractionLayer cal) throws CacheException {
        Optional<String> subcoachInstantiatorId = getSubcoachInstantiatorId(companyId, fqcn, cal);
        if (subcoachInstantiatorId.isPresent()) {
            Answer answer = cal.getAnswer(companyId, fqcn.getRoot(), subcoachInstantiatorId.get());
            return Optional.ofNullable(answer.getCurrentSubcoachInstance());
        }
        return Optional.empty();
    }

    public static List<InstantiatorData> getAllInstantiatorsInCoach(String companyId, FQCN fqcn, CacheAbstractionLayer cal) throws CacheException {
        // First we find all subcoach instantiators in this coach
        CoachLibrary library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        List<String> instantiatorIds = library.getQuestionnaire()
                .getQuestions()
                .getQuestion()
                .stream()
                .filter(q -> q.getType().equals("subcoachInstantiator"))
                .map(Question::getId)
                .collect(Collectors.toList());

        // Get data of instantiators and return it
        List<InstantiatorData> instantiators = new ArrayList<>();
        for (String id : instantiatorIds) {
            instantiators.add(InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, id));
        }
        return instantiators;
    }

    public static List<Tuple<FQCN, Question>> insertSubcoachQuestions(String companyId, FQCN fqcn, CacheAbstractionLayer cal, List<Question> questions) {
        return questions.stream().flatMap(question -> {
            Tuple<FQCN, Question> questionTuple = new Tuple<>(fqcn, question);
            try {
                // If the question is a subcoach outlet we insert all questions of the subcoach at the position of the outlet
                if (question.getType().equals("subcoachInstantiatorOutlet")) {
                    InstantiatorData instantiator = InstantiatorData.ofInstantiatorId(companyId, fqcn, cal, question.getSubcoachInstantiatorId());

                    // Find all questions of the subcoach outlet
                    List<Tuple<FQCN, Question>> subcoachQuestions = new ArrayList<>();
                    for (SubcoachInstances.SubcoachInstance instance : instantiator.getInstances()) {
                        FQCN subcoachFqcn = FQCN.from(fqcn.getRootCoachId(), instantiator.subcoachId, instance.getSubcoachId());
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


    public final static class InstantiatorData {
        private final String instantiatorId;
        private final String subcoachId;
        private final List<SubcoachInstances.SubcoachInstance> instances;

        private InstantiatorData(String instantiatorId, String subcoachId, List<SubcoachInstances.SubcoachInstance> instances) {
            this.instantiatorId = instantiatorId;
            this.subcoachId = subcoachId;
            this.instances = instances;
        }

        public static InstantiatorData ofInstantiatorId(String companyId, FQCN fqcn, CacheAbstractionLayer cal, String instantiatorId) throws CacheException {
            Answer answer = cal.getAnswer(companyId, fqcn.getRoot(), instantiatorId);
            Question instantiatorQuestion = cal.getQuestion(fqcn.getRootCoachId(), instantiatorId);
            return new InstantiatorData(instantiatorId, instantiatorQuestion.getSubcoachId(), answer.getSubcoachInstances().getSubcoachInstance());
        }

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
