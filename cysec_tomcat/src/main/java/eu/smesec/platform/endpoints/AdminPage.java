package eu.smesec.platform.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.auth.SecuredAdmin;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.endpoints.AdminModel;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Logger;

@SecuredAdmin
@Path("rest/admin")
public class AdminPage {
    static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    private final CacheAbstractionLayer cal = CacheAbstractionLayer.getInstance();

    /**
     * Renders the admin overview page.
     *
     * @return rendered admin page.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getAdminPage() {
        AdminModel adminModel = new AdminModel();
        try {
            Map<String, Object> model = adminModel.getAdminModel();
            return Response.status(200).entity(new Viewable("/admin/admin", model)).build();
        } catch (CacheException ce) {
            logger.warning(ce.getMessage());
        }
        return Response.status(500).build();
    }

    /**
     * Renders the admin audits.
     *
     * @param companyId The id of the company
     * @return rendered admin audits
     */
    @GET
    @Path("audits/{cid}")
    @Produces(MediaType.TEXT_HTML)
    public Response getAdminAudits(@PathParam("cid") String companyId) {
        AdminModel adminModel = new AdminModel();

        try {
            Map<String, Object> model = adminModel.getAdminAuditsModel(companyId);
            return Response.status(200).entity(new Viewable("/admin/audits", model)).build();
        } catch (CacheException ce) {
            logger.warning(ce.getMessage());
        }
        return Response.status(500).build();
    }
}
