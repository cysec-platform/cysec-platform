package eu.smesec.platform.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.TokenExpiredException;
import eu.smesec.bridge.generated.Token;
import eu.smesec.bridge.generated.User;
import eu.smesec.core.endpoints.PasswordForgottenModel;
import eu.smesec.core.endpoints.UsersModel;
import eu.smesec.platform.services.MailServiceImpl;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("rest/resetPassword")
@PermitAll
public class PasswordForgottenService {
    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    private static final int tokenExpiryHours = 1;

    @Inject
    private MailServiceImpl mailService;

    /**
     * Creates a new token for a password reset request.
     *
     * @param email     The email to the corresponding user
     * @param companyId the id of the company
     * @return 404 If no user for the given email was found
     */
    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_HTML)
    @Path("/create")
    public Response createToken(@QueryParam("email") String email, @QueryParam("company") String companyId) {
        UsersModel usersModel = new UsersModel();
        User user = null;
        try {
            user = usersModel.getUserByEmail(companyId, email);
        } catch (CacheException e) {
            logger.warning("Error getting user with email " + email + " in company " + companyId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        //TODO: Can the user ever be null without the getUserByEmail throwing an error? If so, the below check is not needed.

//        if (user == null) {
//                logger.warning("Could not find user with email " + email + " in company " + companyId);
//                return Response.status(Response.Status.NOT_FOUND).build();
//            }

        PasswordForgottenModel passwordForgottenModel = new PasswordForgottenModel();
        Token resetToken = passwordForgottenModel.createToken();

        try {
            usersModel.updateUser(companyId, user);
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        sendResetEmail(user, resetToken);

        return Response.status(Response.Status.NO_CONTENT).build();

    }

    /**
     * Sends a reset email including a reset link with a token
     *
     * @param user       Receipient user of the email
     * @param resetToken Generated reset token
     */
    private void sendResetEmail(User user, Token resetToken) {
        final String mailContent = createMailContent(resetToken.getValue());
        mailService.sendMail(user, null, null, "Your password reset token", mailContent);
    }

    private String createMailContent(String resetTokenValue) {
        return "A password reset token was requested for the account registered with your email address.\n\n" +
                "To set a new password, please visit the following website and enter the token:\n" +
                "https://wwwtest.smesec.eu/cysec/public/resetPassword/resetPassword.html\n\n" +
                "Token:\n" + resetTokenValue + "\n";
    }

    /**
     * Check if a given token is valid.
     *
     * @param tokenId   The token registered to a user email.
     * @param password1 the first password
     * @param password2 the second password
     * @param companyId the company id
     * @return A password reset form if a token was found.
     */
    @POST
    @PermitAll
    @Path("verifyToken/{token}")
    @Produces(MediaType.TEXT_HTML)
    public Response verifyToken(
            @PathParam("token") String tokenId,
            @QueryParam("password1") String password1,
            @QueryParam("password2") String password2,
            @QueryParam("company") String companyId) {
        PasswordForgottenModel passwordForgottenModel = new PasswordForgottenModel();
        // Check password inputs
        if ("".equals(password1) || "".equals(password2) || !password1.equals(password2)) {
            logger.warning("Empty or non-matching passwords, returning form");
            return Response.notModified().build();
        }

        // Check for empty token
        if ("".equals(tokenId)) {
            logger.info("No token provided in request");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        //TODO: I noticed that in the createToken method above, the token is not assigned to a user, so how does this work?
        //TODO: Also can the owner ever be null without getUserByToken throwing an error?
        User user = null;
        try {
            user = passwordForgottenModel.verifyToken(tokenId, companyId);
        } catch (TokenExpiredException tee) {
            logger.log(Level.WARNING, tee.getMessage(), tee);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, ce.getMessage(), ce);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Perform password update
        try {
            passwordForgottenModel.updatePassword(password1, user, companyId);
        } catch (CacheException ce) {
            logger.log(Level.WARNING, "Error during update of password: " + ce.getMessage(), ce);
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during update of password: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
