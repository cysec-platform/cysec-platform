package eu.smesec.platform.endpoints;

import eu.smesec.bridge.generated.Company;
import eu.smesec.platform.auth.Secured;
import eu.smesec.platform.auth.SecuredAdmin;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.config.CysecConfig;
import eu.smesec.platform.helpers.dashboard.AdminPageHelper;
import eu.smesec.bridge.generated.Audit;

import eu.smesec.platform.messages.AdminAuditsMsg;
import eu.smesec.platform.messages.AdminMsg;
import eu.smesec.platform.messages.BadgeMsg;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@SecuredAdmin
@Path("rest/admin")
public class AdminPage {
  static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  @Context
  ServletContext context;
  @Inject
  private CacheAbstractionLayer cal;

  /***
   * <p>Return the dashboard for the admin page.</p>
   *
   * @return A JSP representation of the admin page.
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Response getAdminPage() {

    try {
      List<Company> companies = cal.getCompanies();
      AdminMsg msg = new AdminMsg(null, companies.size());
      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("companies", companies);
      return Response.status(200).entity(new Viewable("/admin/admin", model)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return Response.status(500).build();
  }

  @GET
  @Path("audits/{cid}")
  @Produces(MediaType.TEXT_HTML)
  public Response getAdminAudits(@PathParam("cid") String companyId) {
    try {
      List<Audit> audits = cal.getAllAuditLogs(companyId);
      AdminAuditsMsg msg = new AdminAuditsMsg(null, audits.size());
      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("companyId",companyId);
      model.put("audits", audits);
      return Response.status(200).entity(new Viewable("/admin/audits", model)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return Response.status(500).build();
  }

    // Access global config

//    String configString = null;
//    try {
//      configString = CysecConfig.getDefault().store();
//
//
//    } catch (IOException e) {
//      e.printStackTrace();
//    }

//    try {
//      // Fetch per company audit logs
//      Map<String, Audit[]> auditMap = new HashMap<>();
//      for (String companyId : cal.getCompanyIds()) {
//        auditMap.put(companyId, cal.getAllAuditLogs(companyId).toArray(new Audit[0]));
//      }
//      AdminPageHelper model = new AdminPageHelper(configString, auditMap);
//
//      return Response.status(200)
//            .entity(new Viewable("/admin/index", model))
//            .build();
//    } catch (CacheException ce) {
//      logger.warning(ce.getMessage());
//    }
//    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

}
