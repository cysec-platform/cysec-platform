package eu.smesec.core.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.Library;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.bridge.execptions.ElementNotFoundException;
import eu.smesec.bridge.generated.Answer;
import eu.smesec.bridge.generated.Audit;
import eu.smesec.bridge.generated.Metadata;
import eu.smesec.bridge.generated.Mvalue;
import eu.smesec.bridge.generated.Question;
import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.bridge.generated.UserAction;
import eu.smesec.bridge.md.LastSelected;
import eu.smesec.bridge.md.MetadataUtils;
import eu.smesec.bridge.md.State;
import eu.smesec.bridge.utils.AuditUtils;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.LibCal;
import eu.smesec.core.cache.ResourceManager;
import eu.smesec.core.json.MValueAdapter;
import eu.smesec.core.messages.CoachMsg;
import eu.smesec.core.utils.LocaleUtils;
import eu.smesec.core.utils.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.DenyAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@DenyAll
@Path("rest/coaches")
public class Coaches {
  @Context ServletContext context;
  private final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  private final Gson gson =
      new GsonBuilder().registerTypeAdapter(Mvalue.class, new MValueAdapter()).create();
  @Inject private CacheAbstractionLayer cal;
  @Inject private ResourceManager res;

  /**
   * Instantiates a new coach.
   * Triggers Library.onBegin().
   *
   * @param id The id of the coach
   * @return response
   */
  @POST
  @Path("/{id}/instantiate")
  public Response instantiateCoach(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      Questionnaire coach = cal.getCoach(id);
      if (coach != null) {
        cal.instantiateCoach(companyId, coach);
        // set context for answer file per company
        Library library = cal.getLibrariesForQuestionnaire(id).get(0);
        library.onBegin(FQCN.fromString(id));
        return Response.status(200).build();
      }
      return Response.status(404).build();
    } catch (CacheException ce) {
      logger.log(Level.SEVERE, "Error instantiating coach", ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Resumes a coach.
   * Triggers Library.onResume().
   *
   * @param id The if of the coach
   * @return response
   */
  @POST
  @Path("/{id}/resume")
  public Response resumeCoach(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      context.setAttribute("fqcn", id);
      FQCN fqcn = FQCN.fromString(id);
      Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      library.onResume(fqcn.getCoachId(), fqcn);
      // update last selected
      LastSelected lastSelected = new LastSelected(fqcn.toString());
      cal.setMetadataOnAnswers(companyId, LibCal.FQCN_COMPANY, MetadataUtils.toMd(lastSelected));
      return Response.status(200).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Retrieves the first question of a coach.
   *
   * @param id The id of the coach.
   * @return response
   */
  @GET
  @Path("/{id}/questions/first")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getFirst(@PathParam("id") String id) {
    try {
      FQCN fqcn = FQCN.fromString(id);
      Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question first = library.getFirstQuestion();
      return Response.status(200).entity(gson.toJson(first)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Retrieves the last question of a coach.
   *
   * @param id The id of the coach.
   * @return response
   */
  @GET
  @Path("/{id}/questions/last")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getLast(@PathParam("id") String id) {
    try {
      FQCN fqcn = FQCN.fromString(id);
      Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question last = library.getLastQuestion();
      return Response.status(200).entity(gson.toJson(last)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Retrieves the current question of a coach.
   *
   * @param id The id of the coach.
   * @return response
   */
  @GET
  @Path("/{id}/questions/current")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getCurrent(@PathParam("id") String id) {
    String companyId = context.getAttribute("company").toString();
    try {
      FQCN fqcn = FQCN.fromString(id);
      Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question question = library.getFirstQuestion();
      Metadata md = cal.getMetadataOnAnswer(companyId, fqcn, MetadataUtils.MD_STATE);
      if (md != null) {
        State state = MetadataUtils.fromMd(md, State.class);
        question = cal.getQuestion(fqcn.getCoachId(), state.getResume());
      }
      return Response.status(200).entity(gson.toJson(question)).build();
    } catch (CacheException ce) {
      logger.log(Level.SEVERE, "Error occured", ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Updates an answers of an instantiated coach.
   *
   * @param id The fully qualified coach name
   * @param qid The id of the question
   * @param value new answer value
   * @return response
   */
  @POST
  @Path("/{id}/answers/{qid}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response updateAnswer(
      @PathParam("id") String id, @PathParam("qid") String qid, String value) {
    String companyId = context.getAttribute("company").toString();
    String userId = context.getAttribute("user").toString();
    try {
      FQCN fqcn = FQCN.fromString(id);
      String coachId = fqcn.getCoachId();
      Questionnaire coach = cal.getCoach(coachId);
      if (coach == null) {
        logger.warning("cannot find coach " + coachId);
        return Response.status(404).build();
      }
      Library library = cal.getLibrariesForQuestionnaire(coachId).get(0);
      // validate response
      if (!Validator.validateAnswer(value)) {
        logger.warning("response value is invalid");
        return Response.status(400).build();
      }

      // check question exists
      Question question = cal.getQuestion(coachId, qid);
      if (question == null) {
        logger.warning("cannot find question " + qid);
        return Response.status(404).build();
      }
      // handle answer, danger for race-condition for deselect
      synchronized (Coaches.class) {
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
                userId,
                UserAction.MODIFIED,
                before == null ? "" : before,
                after == null ? "" : after);
        cal.createAuditLog(companyId, audit);

        library.onResponseChange(question, answer, fqcn);
      }
      Question next = library.getNextQuestion(question, fqcn);
      List<Question> active = library.peekQuestions(question);

      JsonObject data = new JsonObject();
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
      data.addProperty("next", url);

      // todo active
      return Response.status(200).entity(data.toString()).build();

    } catch (ElementAlreadyExistsException | ElementNotFoundException se) {
      logger.warning(se.getMessage());
      return Response.status(409).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  /**
   * Renders a question.
   *
   * @param id The id of the coach
   * @param questionId the id of the question
   * @return rendered question as html
   */
  @GET
  @Path("/{id}/questions/{qid}/render")
  @Produces(MediaType.TEXT_HTML)
  public Response renderQuestion(@PathParam("id") String id, @PathParam("qid") String questionId) {
    String companyId = context.getAttribute("company").toString();
    Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
    try {
      FQCN fqcn = FQCN.fromString(id);
      context.setAttribute("fqcn", id);
      Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
      Question question = cal.getQuestion(fqcn.getCoachId(), questionId, locale);
      if (question == null) {
        return Response.status(404).build();
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
      return Response.status(200).entity(new Viewable("/coaching/coach", model)).build();
    } catch (CacheException ce) {
      logger.severe(ce.getMessage());
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error occured", e);
    }
    return Response.status(500).build();
  }

  //  @GET
  //  @Path("/{id}/pagination/{qid}/render")
  //  @Produces(MediaType.TEXT_HTML)
  //  public Response renderPagination(@PathParam("id") String id,
  //                                   @PathParam("qid") String questionId) {
  //    String companyId = context.getAttribute("company").toString();
  //    try {
  //      FQCN fqcn = FQCN.fromString(id);
  //      context.setAttribute("fqcn", id);
  //      Library library = cal.getLibrariesForQuestionnaire(fqcn.getCoachId()).get(0);
  //      Question question = cal.getQuestion(fqcn.getCoachId(), questionId);
  //      if (question == null) {
  //        return Response.status(404).build();
  //      }
  //      Answer answer = cal.getAnswer(companyId, fqcn, questionId);
  //      // update state
  //      State state = new State(questionId, null);
  //      cal.setMetadataOnAnswers(companyId, fqcn, MetadataUtils.toMd(state));
  //
  //      Map<String, Object> model = new HashMap<>();
  //      model.put("question", question);
  //      model.put("answer", answer);
  //      model.put("fqcn", id);
  //      model.put("next", library.getNextQuestion(question, fqcn).getId());
  //      model.put("active", library.peekQuestions(question));
  //      return Response.status(200).entity(new Viewable("/coaching/coach", model)).build();
  //    } catch (CacheException ce) {
  //      logger.severe(ce.getMessage());
  //      return Response.status(400).build();
  //    } catch (Exception e) {
  //      logger.log(Level.SEVERE, "Error occured", e);
  //    }
  //    return Response.status(500).build();
  //  }
}
