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

import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.cache.ResourceManager;
import eu.smesec.cysec.platform.core.utils.FileResponse;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.mvc.Viewable;

@PermitAll
@Path("rest/resources")
public class Resources {
  private static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  @Inject
  private CacheAbstractionLayer cache;

  @Inject
  private ResourceManager resManager;

  /**
   * Download a resource file of a coach.
   * Renders *.jsp files.
   *
   * @param coachId The id of the coach
   * @param libId The id of the library
   * @param segments The file path, relative to the coach resource root
   * @param info uri context
   * @return file
   */
  @GET
  @Path("{coach}/{lib}/{any: .*}")
  public Response get(@PathParam("coach") String coachId,
                      @PathParam("lib") String libId,
                      @PathParam("any") List<PathSegment> segments,
                      @Context UriInfo info) {
    try {
      StringBuilder sb = new StringBuilder(coachId).append("/").append(libId);
      for (PathSegment segment : segments) {
        sb.append("/").append(segment.getPath());
      }
      java.nio.file.Path relative = Paths.get(sb.toString());
      String mime = Files.probeContentType(relative);
      if (sb.toString().endsWith(".jsp")) {
        // render jsp file from query params
        CoachLibrary lib = cache.getLibrary(coachId, libId);
        Map<String, Object> model = lib.getJspModel(relative.getParent().toString());
        return Response.status(200)
              .entity(new Viewable("/res_coaches/" + relative.toString(), model))
              .type(MediaType.TEXT_HTML)
              .build();
      }
      FileResponse fd = resManager.getResource(relative);
      if (fd != null) {
        return Response.status(200)
              .entity(fd)
              .type(mime)
              .build();
      }
      return Response.status(404).build();
    } catch (Exception e) {
      logger.severe(e.getMessage());
    }
    return Response.status(500).build();
  }
}
