package eu.smesec.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.Library;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.generated.*;
import eu.smesec.bridge.md.LastSelected;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.bridge.md.State;
import eu.smesec.bridge.utils.AuditUtils;
import eu.smesec.core.auth.AuthConstants;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.cache.ResourceManager;
import eu.smesec.core.json.MValueAdapter;
import eu.smesec.core.messages.CoachMsg;
import eu.smesec.core.utils.LocaleUtils;
import eu.smesec.core.utils.Validator;

import java.util.*;
import java.util.logging.Logger;

public class CoachesModel {

    private final Gson gson =
            new GsonBuilder().registerTypeAdapter(Mvalue.class, new MValueAdapter()).create();

    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();
    private final ResourceManager res = ResourceManager.getInstance();
    private final Logger logger = Logger.getLogger(DataModel.class.getName());

    /**
     * Instantiates a new coach
     *
     * @param id Coach ID
     * @throws CacheException Thrown on failure to get coach, instantiate coach, or load library
     */
    public void instantiateCoach(String id) throws CacheException {
        String companyId = LibCal.getCompany();
        Questionnaire coach = cal.getCoach(id);
        cal.instantiateCoach(companyId, coach);
        Library library = cal.getLibrariesForQuestionnaire(id).get(0);
        library.onBegin(FQCN.fromString(id));
    }

    /**
     * Resumes a coach. Triggers Library.onResume();
     *
     * @param id Coach ID
     * @throws CacheException Thrown on failure to load Library or Metadata
     */
    public void resumeCoach(String id) throws CacheException {
        String companyId = LibCal.getCompany();
//      OLD:  context.setAttribute("fqcn", id);
//      NEW: LibCal.setCoachContext(id);
        LibCal.setCoachContext(id);

        FQCN fqcn = FQCN.fromString(id);
        Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        library.onResume(fqcn.getCoachId(), fqcn);
        // update last selected
        LastSelected lastSelected = new LastSelected(fqcn.toString());
        cal.setMetadataOnAnswers(companyId, LibCal.FQCN_COMPANY, MetadataUtils.toMd(lastSelected));
    }

    /**
     * Gets the First Question of a coach
     *
     * @param id Coach ID
     * @return First question as JSON
     */
    public String getFirst(String id) throws CacheException {
        FQCN fqcn = FQCN.fromString(id);
        Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        Question first = library.getFirstQuestion();
        return gson.toJson(first);
    }

    /**
     * Gets the last question of a coach
     *
     * @param id Coach ID
     * @return Last question as JSON
     */
    public String getLast(String id) throws CacheException {
        FQCN fqcn = FQCN.fromString(id);
        Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        Question last = library.getLastQuestion();
        return gson.toJson(last);
    }

    /**
     * Retrieves the current question of a coach.
     *
     * @param id Coach ID
     * @return Current question as JSON
     */
    public String getCurrent(String id) throws CacheException {
        String companyId = LibCal.getCompany();
        FQCN fqcn = FQCN.fromString(id);
        Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        Question question = library.getFirstQuestion();
        Metadata md = cal.getMetadataOnAnswer(companyId, fqcn, MetadataUtils.MD_STATE);
        if (md != null) {
            State state = MetadataUtils.fromMd(md, State.class);
            question = cal.getQuestion(fqcn.getCoachId(), state.getResume());
        }
        return gson.toJson(question);
    }

    /**
     * Updates an answer of an instantiated coach. Uses the default user.
     *
     * @param id    The fully qualified coach name
     * @param qid   The id of the question
     * @param value new answer value
     * @throws Exception Thrown during read and write operations from and to the cache
     */
    public String updateAnswer(String id, String qid, String value) throws Exception {
        String user = AuthConstants.USER;
        return updateAnswer(id, qid, value, user);
    }

    /**
     * Updates an answers of an instantiated coach.
     *
     * @param id    The fully qualified coach name
     * @param qid   The id of the question
     * @param value new answer value
     * @throws Exception Thrown during read and write operations from and to the cache
     */
    public String updateAnswer(String id, String qid, String value, String user) throws Exception {
        String companyId = LibCal.getCompany();
        FQCN fqcn = FQCN.fromString(id);
        String coachId = fqcn.getCoachId();
        Questionnaire coach = cal.getCoach(coachId);
        if (coach == null) {
            logger.warning("Coach not found, CoachID: " + coachId);
            throw new CacheNotFoundException("Coach not found, CoachID: " + coachId);
        }

        // validate response
        if (!Validator.validateAnswer(value)) {
            logger.warning("Response value is invalid");
            throw new Exception("Response value is invalid");
        }

        // check question exists
        Question question = cal.getQuestion(coachId, qid);
        if (question == null) {
            logger.warning("Cannot find question " + qid);
            throw new Exception("Cannot find question " + qid);
        }

        Library library = cal.getLibrariesForQuestionnaire(coachId).get(0);

        // handle answer, danger for race-condition for deselect
        synchronized (CoachesModel.class) {
            Answer answer = cal.getAnswer(companyId, fqcn, qid);
            String after = "";
            String before = "";
            if (answer != null) {
                // update existing
                if (question.getType().startsWith("Astar")) {
                    // Create new List as Arrays.asList is unmodifiable
                    List<String> options;
                    if (answer.getAidList() == null) {
                        options = new ArrayList<>();
                        // before is already empty, no changing required
                    } else {
                        options = new ArrayList<>(Arrays.asList(answer.getAidList().split(" ")));
                        before = options.toString();
                    }
                    if (options.contains(value)) {
                        options.remove(value);
                    } else {
                        options.add(value);
                    }
                    String newOptions = String.join(" ", options);
                    after = newOptions;
                    answer.setAidList(newOptions);

                } else {
                    before = answer.getText();
                    after = value;
                }
                answer.setText(value); // update the Text field. Indicates last given option
                cal.updateAnswer(companyId, fqcn, answer);
            } else {
                // leave "before on initial empty value"
                // create new Answer
                answer = new Answer();
                answer.setQid(question.getId());
                answer.setText(value);
                if (question.getType().startsWith("Astar")) {
                    answer.setAidList(value);
                    after = answer.getQid();
                } else {
                    after = value;
                }
                cal.createAnswer(companyId, fqcn, answer);
            }

            // Add audit entry
            // Make sure to use empty string if "before" or "after" is null
            Audit audit =
                    AuditUtils.createAudit(
                            user,
                            UserAction.MODIFIED,
                            before == null ? "" : before,
                            after == null ? "" : after);
            cal.createAuditLog(companyId, audit);

            library.onResponseChange(question, answer, fqcn);
        }
        Question next = library.getNextQuestion(question, fqcn);
        List<Question> active = library.peekQuestions(question);

        JsonObject nextURLJSON = new JsonObject();
        String url =
                next != null
                        ? "/app/coach.jsp?fqcn=" + fqcn.toString() + "&question=" + next.getId()
                        : res.hasResource(fqcn.getCoachId(), library.getId(), "/assets/jsp/summary.jsp")
                        ? "/api/rest/resources/"
                        + fqcn.getCoachId()
                        + "/"
                        + library.getId()
                        + "/assets/jsp/summary.jsp"
                        : "/app";
        nextURLJSON.addProperty("next", url);

        // todo active
        return nextURLJSON.toString();
    }

    /**
     * Return the question model. Uses default locale
     *
     * @param id         The id of the coach
     * @param questionId the id of the question
     * @return Question Model
     */
    public Map<String, Object> getQuestionModel(String id, String questionId) throws Exception {
        Locale locale = LocaleUtils.fromString(null);
        return getQuestionModel(id, questionId, locale);
    }

    /**
     * Renders a question.
     *
     * @param id         The id of the coach
     * @param questionId the id of the question
     * @return Question Model
     */
    public Map<String, Object> getQuestionModel(String id, String questionId, Locale locale) throws Exception {
        String companyId = LibCal.getCompany();
        FQCN fqcn = FQCN.fromString(id);
        //TODO: getAttribute("fqcn"... is never called or used. Is it needed?
//        context.setAttribute("fqcn", id);
        Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
        Question question = cal.getQuestion(fqcn.getCoachId(), questionId, locale);
        if (question == null) {
            throw new Exception("Could not find question");
        }
        // update state
        State state = new State(questionId, null);
        cal.setMetadataOnAnswers(companyId, fqcn, MetadataUtils.toMd(state));

        // next
        Question next = library.getNextQuestion(question, fqcn);
        String url =
                next != null
                        ? "/app/coach.jsp?fqcn=" + fqcn.toString() + "&question=" + next.getId()
                        : res.hasResource(fqcn.getCoachId(), library.getId(), "/assets/jsp/summary.jsp")
                        ? "/api/rest/resources/"
                        + fqcn.getCoachId()
                        + "/"
                        + library.getId()
                        + "/assets/jsp/summary.jsp"
                        : "/app";

        CoachMsg msg = new CoachMsg(locale);
        Map<String, Object> model = new HashMap<>();
        model.put("msg", msg.getMessages());
        model.put("question", question);
        model.put("answer", cal.getAnswer(companyId, fqcn, questionId));
        model.put("fqcn", id);
        model.put("next", url);
        model.put("actives", library.peekQuestions(question));
        return model;
    }
}