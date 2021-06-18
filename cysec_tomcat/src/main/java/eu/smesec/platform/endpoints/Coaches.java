package eu.smesec.platform.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.core.endpoints.CoachesModel;
import eu.smesec.core.utils.LocaleUtils;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.security.DenyAll;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@DenyAll
@Path("rest/coaches")
public class Coaches {
    @Context
    ServletContext context;
    private final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);


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
        CoachesModel coachesModel = new CoachesModel();
        try {
            coachesModel.instantiateCoach(id);
        } catch (CacheException ce) {
            logger.log(Level.SEVERE, "Error: Could not instantiate coach", ce);
            return Response.status(500).build();
        }
        return Response.status(200).build();
    }

    /**
     * Resumes a coach.
     * Triggers Library.onResume().
     *
     * @param id Coach ID
     * @return response
     */
    @POST
    @Path("/{id}/resume")
    public Response resumeCoach(@PathParam("id") String id) {
        CoachesModel coachesModel = new CoachesModel();
        try {
            coachesModel.resumeCoach(id);
        } catch (CacheException ce) {
            logger.log(Level.SEVERE, "Error: Could not resume coach", ce);
            return Response.status(500).build();
        }
        return Response.status(200).build();
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
        CoachesModel coachesModel = new CoachesModel();
        try {
            String firstQuestion = coachesModel.getFirst(id);
            return Response.status(200).entity(firstQuestion).build();
        } catch (CacheException ce) {
            logger.log(Level.SEVERE, "Error: Could not get first question", ce);
            return Response.status(500).build();
        }
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
        CoachesModel coachesModel = new CoachesModel();
        try {
            String last = coachesModel.getLast(id);
            return Response.status(200).entity(last).build();
        } catch (CacheException ce) {
            logger.log(Level.SEVERE, "Error: Could not get last question", ce);
            return Response.status(500).build();
        }
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
        CoachesModel coachesModel = new CoachesModel();
        try {
            String current = coachesModel.getCurrent(id);
            return Response.status(200).entity(current).build();
        } catch (CacheException ce) {
            logger.log(Level.SEVERE, "Error: Could not get current question", ce);
            return Response.status(500).build();
        }
    }

    /**
     * Updates an answers of an instantiated coach.
     *
     * @param id    The fully qualified coach name
     * @param qid   The id of the question
     * @param value new answer value
     * @return response
     */
    @POST
    @Path("/{id}/answers/{qid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAnswer(
            @PathParam("id") String id, @PathParam("qid") String qid, String value) {
        //TODO: Find usage setAttribute("user"). Where should we store the user?
        String userId = context.getAttribute("user").toString();
        CoachesModel coachesModel = new CoachesModel();
        try {
            String nextURLJSON = coachesModel.updateAnswer(id, qid, value, userId);
            return Response.status(200).entity(nextURLJSON).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not update answer. The following error occured", e);
            return Response.status(404).build();
        }
    }

    /**
     * Renders a question.
     *
     * @param id         The id of the coach
     * @param questionId the id of the question
     * @return rendered question as html
     */
    @GET
    @Path("/{id}/questions/{qid}/render")
    @Produces(MediaType.TEXT_HTML)
    public Response renderQuestion(@PathParam("id") String id, @PathParam("qid") String questionId) {
        CoachesModel coachesModel = new CoachesModel();
        Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
        try {
            Map<String, Object> questionModel = coachesModel.getQuestionModel(id, questionId, locale);
            return Response.status(200).entity(new Viewable("/coaching/coach", questionModel)).build();
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
