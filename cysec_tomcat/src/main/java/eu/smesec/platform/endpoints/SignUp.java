package eu.smesec.platform.endpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.execptions.ElementAlreadyExistsException;
import eu.smesec.core.endpoints.SignUpModel;
import eu.smesec.core.json.FieldsExclusionStrategy;
import eu.smesec.platform.services.MailServiceImpl;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@PermitAll
@Path("rest/signUp")
public class SignUp {
    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    private static final Gson addUserGson =
            new GsonBuilder()
                    .addDeserializationExclusionStrategy(
                            new FieldsExclusionStrategy("id", "lock", "roles", "token"))
                    .create();
    @Inject
    private MailServiceImpl mailService;
    @Context
    private ServletContext context;

    /**
     * Renders the sign up form template.
     *
     * @return An html form with all available companies populated into a dropdown.
     */
    @GET
    @Path("/user")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public Response getUserSignUpForm() {
        SignUpModel signUpModel = new SignUpModel();
        try {
            Map<String, Object> model = signUpModel.getUserSignUpFormModel();
            return Response.status(200).entity(new Viewable("/signUpForm", model)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return Response.status(500).build();
    }

    /**
     * Creates a new user.
     *
     * @param json      user data as json
     * @param companyId company id
     * @return response
     */
    @POST
    @Path("/user")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(String json, @QueryParam("company") String companyId) {
        if (json == null) {
            logger.log(Level.WARNING, "user json is null");
            return Response.status(400).build();
        }
        if (companyId == null) {
            logger.log(Level.WARNING, "company id is null");
            return Response.status(400).build();
        }
        SignUpModel usersModel = new SignUpModel();
        try {
            Long userID = usersModel.createUser(json);
            // Get company admins and send notification email
//            List<User> admins = cal.getAllAdminUsers(companyId);
            // enable on production
            // mailService.sendMail(admins, "", "",
            //        "New user request", "Approve user: " + user.getUsername());
            return Response.status(200).entity(userID).build();
        } catch (CacheNotFoundException nfe) {
            logger.log(Level.WARNING, nfe.getMessage(), nfe);
            return Response.status(400).build();
        } catch (ElementAlreadyExistsException aee) {
            logger.log(Level.WARNING, aee.getMessage(), aee);
            return Response.status(409).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to create user", e);
        }
        return Response.status(500).build();
    }

    /**
     * Renders the sign-up form.
     *
     * @return rendered sign-up form
     */
    @GET
    @Path("/company")
    @PermitAll
    @Produces(MediaType.TEXT_HTML)
    public Response getCompanySignUpForm() {
        SignUpModel signUpModel = new SignUpModel();
        try {
            Map<String, Object> model = signUpModel.getCompanySignUpFormModel();
            return Response.status(200).entity(new Viewable("/signUpForm", model)).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating signup form: " + e.getMessage(), e);
        }
        return Response.status(500).build();
    }

    /**
     * Creates a new company.
     * The user will be the first company admin.
     *
     * @param json        the data of the company
     * @param companyId   the id of the company
     * @param companyName the name of the company
     * @return response
     */
    @POST
    @Path("/company")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCompany(
            String json, @QueryParam("id") String companyId, @QueryParam("name") String companyName) {
        if (json == null) {
            logger.log(Level.WARNING, "user json is null");
            return Response.status(400).build();
        }
        if (companyId == null || companyName == null) {
            logger.log(Level.WARNING, "company id or name is null");
            return Response.status(400).build();
        }
        SignUpModel signUpModel = new SignUpModel();
        try {
            Long adminID = signUpModel.createCompany(json, companyId, companyName);
            return Response.status(200).entity(adminID).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error creating company: " + e.getMessage(), e);
        }
        return Response.status(500).build();
    }
}
