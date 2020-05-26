package eu.smesec.bridge;

import eu.smesec.bridge.generated.Answer;
import eu.smesec.bridge.generated.Block;
import eu.smesec.bridge.generated.Question;
import eu.smesec.bridge.generated.Questionnaire;

import eu.smesec.bridge.md.MetadataUtils;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The heart of the communication between Library and Platform.
 * <p>The library offers methods to trigger Scoring, Adding/Removing of Metadata and Hiding/Showing questions.
 * Apart from that, the some lifecycle methods</p>
 */
public interface Library {

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
   * The initial routine run by the coach when instantiated. This method is executed _ONCE_ in the lifecycle of a coach.
   * <p>This will invoke the onBegin logic block in the coach that usually sets up all badges and badge classes.
   * Also, it saves default values (0) for strength, knowhow and uu score and their max values (0) in metadata.</p>
   * @return A list of commands to execute on the frontend
   */
  List<Command> onBegin(FQCN fqcn);

  /**
   * The routine invoked every time a client access a different coach.
   * <p>This method also takes care of recovering answers from metadata by reapplying all saved answers to
   * the onResponseChange routine</p>
   * @param questionId the Id of the question from which to resume
   * @see #onResponseChange(Question, Answer, FQCN)
   * @return A list of commands to execute on the frontend
   */
  List<Command> onResume(String questionId, FQCN fqcn);

  /**
   * A routine executed every time a client adds text to question or selects an option.
   * <p>The library will evaluate the question that will update scoring, add metadata or hide questions</p>
   * @param question the question that was answered
   * @param answer the answer that was given
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

  /**
   * Ask library to (re)evaluate the current question and return the next question.
   * @param question the question that should be evaluated
   * @return the question that should be displayed next
   */
  Question getNextQuestion(Question question, FQCN fqcn);

  /**
   * Ask the library for the current list of active questions.
   * @param question
   * @return A list of question objects to display
   */
  List<Question> peekQuestions(Question question);

  /**
   * Ask the library for the last question of the coach.
   * @return A question object or null (if coach has no questions)
   */
  Question getLastQuestion();

  /**
   * Ask the library for the first question of the coach.
   * @return A Question object or null (if coach has no questions)
   */
  Question getFirstQuestion();

  /**
   * Sets the given object as its parent.
   *
   * <p>The Library knows this object must be type of CysecExecutorContext and perform the appropriate type cast.</p>
   * <p>It is only possible to set the parent once to avoid state changes.</p>
   * @throws if attempted to set parent more than once or set itself as parent
   */
  void setParent(Object context) throws IllegalStateException;

  /**
   * Get the parent executor context
   *
   * <p>The client must treat this object as an instance of <TT>CysecExecutorContext</TT>. However, since that class
   * is not known on platform side, it is not possible to perform the appropriate type cast.</p>
   *
   * @return the requested context
   */
  Object getParent();

  /**
   * Gets the model for a *.jsp file.
   *
   * @param file The path of the *.jsp file.
   * @return The model to pass to the *.jsp file.
   */
  default Map<String, Object> getJspModel(String file) {
    return null;
  };
}
