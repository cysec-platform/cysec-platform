package eu.smesec.core.endpoints;

import eu.smesec.core.auth.SecuredReplica;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.utils.FileResponse;
import eu.smesec.core.utils.PathSegmentUtils;

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
