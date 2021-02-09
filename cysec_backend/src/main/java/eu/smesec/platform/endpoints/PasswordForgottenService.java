package eu.smesec.platform.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.TokenExpiredException;
import eu.smesec.bridge.generated.Token;
import eu.smesec.bridge.generated.User;
import eu.smesec.bridge.utils.TokenUtils;
import eu.smesec.platform.auth.CryptPasswordStorage;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.services.MailServiceImpl;

import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

@Path("rest/resetPassword")
@PermitAll
public class PasswordForgottenService {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  private static final int tokenExpiryHours = 1;

  @Context ServletContext context;
  @Inject private CacheAbstractionLayer cal;
  @Inject private MailServiceImpl mailService;

  /**
   * Creates a new token for a password reset request.
   *
   * @param email The email to the corresponding user
   * @return 404 If no user for the given email was found
   */
  @POST
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.TEXT_HTML)
  @Path("/create")
  public Response createToken(
      @QueryParam("email") String email, @QueryParam("company") String companyId) {
    try {
      User user = cal.getUserByEmail(companyId, email);
      if (user == null) {
        logger.warning("Couldn't find user with email " + email + " in company " + companyId);
        return Response.status(404).build();
      }

      // create new token for user
      Token resetToken =
          TokenUtils.createToken(
              TokenUtils.TOKEN_RESET,
              TokenUtils.generateRandomHexToken(16),
              LocalDateTime.now().plusDays(tokenExpiryHours));

      logger.info("Created new password reset token for " + email);
      // send email with token
      logger.info("Token is: " + resetToken.getId());
      // update user
      cal.updateUser(companyId, user);
      // send mail with token
      mailService.sendMail(
          user,
          "",
          "",
          "Your password reset token",
          "Access the following website and use the token below to set a new password: https://wwwtest.smesec.eu/cysec/public/resetPassword/resetPassword.html "
              + "\ntoken:"
              + resetToken.getValue());
      // 10dbe52948c0ab89d7988240a25b4802

      return Response.status(204).build();
    } catch (CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Check if a given token is valid.
   *
   * @param tokenId The token registered to a user email.
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
    try {
      // Check password inputs
      if ("".equals(password1) || "".equals(password2) || !password1.equals(password2)) {
        logger.warning("Empty or non-matching passwords, returning form");
        return Response.notModified().build();
      }

      // Check for empty token
      if ("".equals(tokenId)) {
        logger.info("No token provided in request");
        return Response.status(400).build();
      }

      User owner = cal.getUserByToken(companyId, tokenId);
      if (owner == null) {
        return Response.status(404).build();
      }

      // Perform password update
      CryptPasswordStorage passwordStorage = new CryptPasswordStorage(password1, null);
      owner.setPassword(passwordStorage.getPasswordStorage());
      owner.getToken().removeIf(token -> token.getId().equals("reset"));
      cal.updateUser(companyId, owner);
      return Response.status(204).build();
    } catch (TokenExpiredException tee) {
      logger.log(Level.WARNING, tee.getMessage(), tee);
      return Response.status(401).build();
    } catch (CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error during update of password: " + e.getMessage());
    }
    return Response.status(500).build();
  }
}
