package eu.smesec.platform.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.core.endpoints.DashboardModel;
import eu.smesec.core.utils.LocaleUtils;
import eu.smesec.platform.auth.Secured;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.annotation.security.DenyAll;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Secured
@DenyAll
@Path("rest/dashboard")
public class Dashboard {
    public static final String RECOMMENDATIONS_SIZE = "cysec_recommend_count";

    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    @Context
    private ServletContext context;

    /**
     * Renders the questionnaire list for a company.
     *
     * @return The rendered html document.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboard() {
        logger.info("Retrieving dashboard");
        Locale locale = LocaleUtils.fromString(context.getAttribute("locale").toString());
        String contextName = context.getContextPath().substring(1);

        int recommendationLimit = CysecConfig.getDefault().getNumericValue(contextName, RECOMMENDATIONS_SIZE);

        try {
            Map<String, Object> model = new DashboardModel(recommendationLimit, locale).getModel();
            return Response.status(200).entity(new Viewable("/dashboard/dashboard", model)).build();
        } catch (CacheException ce) {
            logger.log(Level.WARNING, "Error getting dashboard", ce);
            return Response.status(500).build();
        }
    }
}
