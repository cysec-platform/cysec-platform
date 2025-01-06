/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.core.auth.Secured;

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
