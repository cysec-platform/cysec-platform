/*-
 * #%L
 * CYSEC Platform Bridge
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
package eu.smesec.cysec.platform.bridge;

import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Question;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.utils.Tuple;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The heart of the communication between Library and Platform.
 * <p>The library offers methods to trigger Scoring, Adding/Removing of Metadata and Hiding/Showing questions.
 * Apart from that, the some lifecycle methods</p>
 */
public interface CoachLibrary {

  Questionnaire questionnaire = null;

  /**
   * The initialization routine which must be run after creating a new Library instance.
   * <p>Prepares internal state to run life cycle methods called by the platform</p>
   * @param id the id of the library
   * @param questionnaire the coach object that belongs to the library
   * @param libCal the persistence layer abstraction for metadata access
   * @param logger the instance of the logger from the web application context
   */
  void init(String id, Questionnaire questionnaire, ILibCal libCal, Logger logger);

  /**
   * <p>The initial routine run by the coach when instantiated. This method is executed _ONCE_ in the lifecycle of a coach.</p>
   *
   * <p>This will invoke the onBegin logic block in the coach that usually sets up all badges and badge classes.
   * Also, it saves default values (0) for strength, knowhow and uu score and their max values (0) in metadata.</p>
   *
   * @param fqcn unknown? FIXME
   * @return A list of commands to execute on the frontend
   */
  List<Command> onBegin(FQCN fqcn);

  /**
   * <p>The routine invoked every time a client access a different coach.</p>
   *
   * <p>This method also takes care of recovering answers from metadata by reapplying all saved answers to
   * the onResponseChange routine</p>
   *
   * @param questionId the Id of the question from which to resume
   * @param fqcn unknown? FIXME
   * @return A list of commands to execute on the frontend
   *
   * @see #onResponseChange(Question, Answer, FQCN)
   */
  List<Command> onResume(String questionId, FQCN fqcn);

  /**
   * <p>A routine executed every time a client adds text to question or selects an option.</p>
   *
   * <p>The library will evaluate the question that will update scoring, add metadata or hide questions</p>
   * @param question the question that was answered
   * @param answer the answer that was given
   * @param fqcn unknown? FIXME
   *
   * @return A list of commands to execute on the frontend
   */
  List<Command> onResponseChange(Question question, Answer answer, FQCN fqcn);

  String getId();

  default Questionnaire getQuestionnaire() {
    return questionnaire;
  }

  @Deprecated
  InputStream getResource(List<String> segments);

  @Deprecated
  void setQuestionnaire(Questionnaire questionnaire);

  @Deprecated
  void setCal(ILibCal cal);

  @Deprecated
  void setId(String id);

  String getActiveInstance();

  void setActiveInstance(String instance);

  /**
   * <p>Ask library to (re)evaluate the current question and return the next question.</p>
   *
   * @param question the question that should be evaluated
   * @param fqcn the fully qualified coach name to be queried
   *
   * @return the question that should be displayed next
   */
  Question getNextQuestion(Question question, FQCN fqcn);

  /**
   * <p>Ask the library for the current list of active questions.</p>
   *
   * @param question the question to look up
   * @return A list of question objects to display
   */
  List<Question> peekQuestions(Question question);

  /**
   * Get all active questions of a certain (sub)coach.
   * @param fqcn The FQCN of the coach to get the active questions from.
   * @return A list of question IDs of the requested coach. This does not include
   * question IDs of subcoach questions, only the question ID of the outlet is included.
   */
  List<String> getActiveQuestions(FQCN fqcn);

  /**
   * Return the active questions of a coach including its subcoaches.
   * @param fqcn The fqcn of the parent coach
   * @return List of tuples containing the fqcn of the coach where the
   * question comes from and the question itself
   */
  List<Tuple<FQCN, Question>> peekQuestionsIncludingSubcoaches(FQCN fqcn);

  /**
   * Ask the library for the last question of the coach.
   * @return A question object or null (if coach has no questions)
   */
  Question getLastQuestion();

  /**
   * Ask the library for the first question of the coach.
   * @return A Question object or null (if coach has no questions)
   */
  Question getFirstQuestion(String instanceName);

  /**
   * <p>Sets the given object as its parent.</p>
   *
   * <p>The Library knows this object must be type of CysecExecutorContext and perform the appropriate type cast.</p>
   * <p>It is only possible to set the parent once to avoid state changes.</p>
   *
   * @param context the context to be queried
   *
   * @throws IllegalStateException if attempted to set parent more than once or set itself as parent
   */
  void setParent(Object context) throws IllegalStateException;

  /**
   * <p>Get the parent executor context.</p>
   *
   * <p>The client must treat this object as an instance of <code>CysecExecutorContext</code>. However, since that class
   * is not known on platform side, it is not possible to perform the appropriate type cast.</p>
   *
   * @return the requested context
   */
  Object getParent();

  /**
   * Exposed the executor context of the coach library. This has return type object because
   * the executor context type is not available in this project. Any consumer of this method
   * must cast the object in order to do something useful with it.
   * @return ExecutorContext of coachLibrary
   */
  Object getContext();

  /**
   * Updates the active questions pool based on the current visibility state
   * of the questions.
   */
  void updateActiveQuestions(FQCN fqcn);

  /**
   * <p>Gets the model for a *.jsp file.</p>
   *
   * @param file The path of the *.jsp file.
   * @return The model to pass to the *.jsp file.
   */
  default Map<String, Object> getJspModel(String file) {
    return null;
  }
}
