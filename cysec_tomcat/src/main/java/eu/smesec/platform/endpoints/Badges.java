package eu.smesec.platform.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.auth.Secured;
import eu.smesec.core.endpoints.BadgesModel;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.security.DenyAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Logger;

@Secured
@DenyAll
@Path("rest/badges")
public class Badges {
    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    /**
     * Renders the badge overview page.
     *
     * @return rendered badge page
     */
    @GET
    @Path("/render")
    @Produces(MediaType.TEXT_HTML)
    public Response renderAllBadges() {
        BadgesModel badgesModel = new BadgesModel();
        try {
            Map<String, Object> model = badgesModel.getModel();
            return Response.status(200).entity(new Viewable("/all_badges", model)).build();
        } catch (CacheException ce) {
            logger.warning(ce.getMessage());
        }
        return Response.status(500).build();
    }
}
