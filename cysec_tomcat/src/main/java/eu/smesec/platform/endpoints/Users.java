package eu.smesec.platform.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.bridge.execptions.ElementNotFoundException;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.endpoints.UsersModel;
import eu.smesec.core.json.FieldsExclusionStrategy;
import eu.smesec.core.utils.LocaleUtils;
import eu.smesec.platform.auth.Secured;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Secured
@DenyAll
@Path("rest/users")
public class Users {
    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    private static final Gson addUserGson =
            new GsonBuilder()
                    .addDeserializationExclusionStrategy(new FieldsExclusionStrategy("id", "token"))
                    .create();
    private static final Gson getUserGson =
            new GsonBuilder()
                    .addSerializationExclusionStrategy(new FieldsExclusionStrategy("password"))
                    .create();
    private static final Gson updateUserGson = new GsonBuilder().create();

    @Inject
    private CacheAbstractionLayer cal;
    @Context
    ServletContext context;

    /**
     * Renders the user table.
     *
     * @return rendered user table
     */
    @GET
    @Path("/render")
    @Produces(MediaType.TEXT_HTML)
    public Response getRenderedUsers() {
        Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
        UsersModel usersModel = new UsersModel();
        try {
            Map<String, Object> model = usersModel.getUserModel(locale);
            return Response.status(200).entity(new Viewable("/users/users.jsp", model)).build();
        } catch (CacheException ce) {
            logger.warning(ce.getMessage());
            return Response.status(400).build();
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
        return Response.status(500).build();
    }

    /**
     * Creates a new user.
     *
     * @param json user data
     * @return response
     */
    @POST
    @RolesAllowed("Admin")
    public Response createUser(String json) {
        if (json == null) {
            logger.log(Level.WARNING, "user json is null");
            return Response.status(400).build();
        }
        UsersModel usersModel = new UsersModel();
        try {
            Long userID = usersModel.createUser(json);
            return Response.status(200).entity(userID).build();
        } catch (ElementAlreadyExistsException aee) {
            logger.log(Level.WARNING, aee.getMessage(), aee);
            return Response.status(409).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
            return Response.status(400).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create user", e);
        }
        return Response.status(500).build();
    }

    /**
     * Returns an user as json.
     *
     * @param userId The id of the user
     * @return json
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") long userId) {
        UsersModel usersModel = new UsersModel();
        try {
            String user = usersModel.getUser(userId);
            if (user != null) {
                return Response.status(200).entity(user).build();
            }
            return Response.status(404).build();
        } catch (CacheNotFoundException nfe) {
            logger.log(Level.WARNING, nfe.getMessage());
            return Response.status(400).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return Response.status(500).build();
    }

    /**
     * Updates an existing user.
     *
     * @param userId The id of the user
     * @param json   The new user data, the data must contain unchanged data as well!
     * @return response
     */
    @PUT
    @Path("/{id}")
    @RolesAllowed("Admin")
    public Response updateUser(@PathParam("id") long userId, String json) {
        if (json == null) {
            logger.log(Level.WARNING, "user json is null");
            return Response.status(400).build();
        }
        UsersModel usersModel = new UsersModel();
        try {
            usersModel.updateUser(userId, json);
            return Response.status(200).entity(userId).build();
        } catch (CacheNotFoundException nfe) {
            //      logger.log(Level.WARNING, nfe.getMessage(), nfe);
            logger.log(Level.WARNING, nfe.getMessage());
            return Response.status(400).build();
        } catch (ElementNotFoundException | ElementAlreadyExistsException ee) {
            //      logger.log(Level.WARNING, ee.getMessage(), ee);
            logger.log(Level.WARNING, ee.getMessage());
            return Response.status(409).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create user", e);
        }
        return Response.status(500).build();
    }

    /**
     * Deletes an existing user.
     *
     * @param userId The id of the user
     * @return response
     */
    @DELETE
    @Path("/{id}")
    @RolesAllowed("Admin")
    public Response deleteUser(@PathParam("id") long userId) {
        UsersModel usersModel = new UsersModel();
        try {
            usersModel.deleteUser(userId);
            return Response.status(204).build();
        } catch (CacheNotFoundException nfe) {
            //      logger.log(Level.WARNING, nfe.getMessage(), nfe);
            logger.log(Level.WARNING, nfe.getMessage());
            return Response.status(400).build();
        } catch (ElementNotFoundException aee) {
            //      logger.log(Level.WARNING, aee.getMessage(), aee);
            logger.log(Level.WARNING, aee.getMessage());
            return Response.status(404).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to delete user", e);
        }
        return Response.status(500).build();
    }
}
