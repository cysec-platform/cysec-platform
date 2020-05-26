package eu.smesec.platform.endpoints;

import java.util.logging.Logger;
import javax.annotation.security.DenyAll;
import javax.servlet.ServletContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import eu.smesec.platform.auth.Secured;
import org.glassfish.jersey.logging.LoggingFeature;


@Secured
@DenyAll
@Path("rest/login")
public class Login {

  @Context
  ServletContext context;

  private Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  /***
   * <p>Post handler for login.</p>
   * @return Response for post request
   */
  @POST
  public Response login() {
    logger.info("Received a login request which made it through the auth filter.");
    if (context.getAttribute("user") != null) {
      logger.info("User successfully logged in.");
      return Response.ok().entity("Logged in as user " + (context.getAttribute("user"))).build();
    }
    logger.info("User did not log in successfully.");
    return Response.status(Response.Status.UNAUTHORIZED).build();
  }
}
