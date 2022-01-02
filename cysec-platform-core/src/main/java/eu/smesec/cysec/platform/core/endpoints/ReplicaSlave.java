/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.utils.FileResponse;
import eu.smesec.cysec.platform.core.utils.PathSegmentUtils;
import eu.smesec.cysec.platform.core.auth.SecuredReplica;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

/** Endpoint for synchronization from a external server. */
@SecuredReplica
@Path("replica/slave")
public class ReplicaSlave {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  @Context private ServletContext context;
  @Inject private CacheAbstractionLayer cal;

  /**
   * Zips the company and marks it as readonly.
   *
   * @return zipped company
   */
  @GET
  @Path("/clone")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response zip() {
    String companyId = context.getAttribute("company").toString();
    try {
      logger.log(Level.INFO, "Zipping company " + companyId);
      FileResponse fd = cal.zipCompany(companyId);
      cal.setCompanyReadonly(companyId, true);
      logger.log(Level.INFO, "Downloading company " + companyId);
      return Response.status(200).entity(fd).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Handles the file upload to this company.
   *
   * @param inputStream data of the file
   * @param segments path of the file
   * @return response
   */
  @POST
  @Path("/file/{any: .*}")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  public Response upload(InputStream inputStream, @PathParam("any") List<PathSegment> segments) {
    String companyId = context.getAttribute("company").toString();
    try {
      String relative = PathSegmentUtils.combine(segments);
      logger.log(Level.INFO, "Uploading file: " + companyId + "/" + relative);
      cal.syncFile(companyId, Paths.get(relative), inputStream, false);
      logger.log(Level.INFO, "Finished uploading file: " + companyId + "/" + relative);
      return Response.status(204).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Handles the file download from this company.
   *
   * @param segments path of the file
   * @return file
   */
  @GET
  @Path("/file/{any: .*}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response download(@PathParam("any") List<PathSegment> segments) {
    String companyId = context.getAttribute("company").toString();
    try {
      String relative = PathSegmentUtils.combine(segments);
      java.nio.file.Path relPath = Paths.get(relative);
      logger.log(Level.INFO, "Preparing file: " + companyId + "/" + relative + " for download");
      FileResponse fd = cal.createFileResponse(companyId, relPath);
      if (fd != null) {
        logger.log(Level.INFO, "Downloading File: " + companyId + "/" + relative);
        return Response.status(200).entity(fd).type(Files.probeContentType(relPath)).build();
      }
      logger.log(Level.WARNING, "File: " + companyId + "/" + relative + " not found");
      return Response.status(404).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }
}
