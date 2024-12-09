/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.endpoints;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Audit;
import eu.smesec.cysec.platform.bridge.generated.Company;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.messages.AdminAuditsMsg;
import eu.smesec.cysec.platform.core.messages.AdminMsg;
import eu.smesec.cysec.platform.core.auth.SecuredAdmin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
  @Context ServletContext context;
  @Inject private CacheAbstractionLayer cal;

  /**
   * Renders the admin overview page.
   *
   * @return rendered admin page.
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
    try {
      List<Audit> audits = cal.getAllAuditLogs(companyId);
      AdminAuditsMsg msg = new AdminAuditsMsg(null, audits.size());
      Map<String, Object> model = new HashMap<>();
      model.put("msg", msg.getMessages());
      model.put("companyId", companyId);
      model.put("audits", audits);
      return Response.status(200).entity(new Viewable("/admin/audits", model)).build();
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return Response.status(500).build();
  }
}
