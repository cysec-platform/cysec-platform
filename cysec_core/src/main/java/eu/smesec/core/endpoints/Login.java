package eu.smesec.core.endpoints;

import eu.smesec.core.auth.Secured;

import java.util.logging.Logger;
import javax.annotation.security.DenyAll;
import javax.servlet.ServletContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

@Secured
@DenyAll
@Path("rest/login")
public class Login {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  @Context ServletContext context;

  /**
   * Post handler for login.
   *
   * @return Response for post request
   */
  @POST
  public Response login() {
    if (context.getAttribute("user") != null) {
      logger.info("User successfully logged in.");
      return Response.ok().entity("Logged in as user " + (context.getAttribute("user"))).build();
    }
    logger.info("User did not log in successfully.");
    return Response.status(Response.Status.UNAUTHORIZED).build();
  }
}
