package eu.smesec.platform.endpoints;


import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.auth.Secured;
import eu.smesec.core.endpoints.DataModel;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data endpoint to provide data for external services.
 */
@Secured
@Path("rest/data")
public class Data {
    private final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    /**
     * Delivers answers of a questionnaire in JSON format.
     *
     * @param id question id
     * @return A JSON object that contains <code>Answers</code> from the coach
     */
    @GET
    @Path("/{id}/answers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAnswers(@PathParam("id") String id) {
        DataModel dataModel = new DataModel();
        try {
            String allAnswers = dataModel.getAnswers(id);
            return Response.ok().entity(allAnswers).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
        }
        return Response.status(500).build();
    }

    /**
     * Delivers rating of a questionnaire in JSON format.
     *
     * @param id question id
     * @return A JSON object that contains <code>rating</code> from the coach
     */
    @GET
    @Path("/{id}/rating")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRating(@PathParam("id") String id) {
        DataModel dataModel = new DataModel();
        try {
            String rating = dataModel.getRating(id);
            return Response.ok().entity(rating).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
        }
        return Response.status(500).build();
    }

    /**
     * Delivers all recommendations of a questionnaire in JSON format.
     *
     * @return A JSON object that contains <code>Metadata</code> from the coach
     */
    @GET
    @Path("/recommendations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecommendations() {
        DataModel dataModel = new DataModel();

        try {
            String recommendations = dataModel.getRecommendations();
            return Response.ok().entity(recommendations).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
        }
        return Response.status(500).build();
    }

    /**
     * Delivers the skills of a company in JSON format.
     *
     * @param id question id
     * @return A JSON object that contains the Skills <code>Metadata</code>
     */
    @GET
    @Path("/{id}/skills")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSkills(@PathParam("id") String id) {
        DataModel dataModel = new DataModel();
        try {
            String skills = dataModel.getSkills(id);
            return Response.ok().entity(skills).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
        }
        return Response.status(500).build();
    }

    /**
     * Returns a list of all instantiated coaches as JSON.
     *
     * @return A JSON object that contains all instantiated coaches
     */
    @GET
    @Path("/coaches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCoaches() {
        DataModel dataModel = new DataModel();
        try {
            String coaches = dataModel.getCoaches();
            return Response.ok().entity(coaches).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
        }
        return Response.status(500).build();
    }
}
