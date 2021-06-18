package eu.smesec.platform.endpoints;

import eu.smesec.core.endpoints.ReplicaSlaveModel;
import eu.smesec.platform.auth.SecuredReplica;
import eu.smesec.platform.utils.FileResponse;
import eu.smesec.platform.utils.PathSegmentUtils;
import org.glassfish.jersey.logging.LoggingFeature;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Endpoint for synchronization from a external server.
 */
@SecuredReplica
@Path("replica/slave")
public class ReplicaSlave {
    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

    @Context
    private ServletContext context;


    /**
     * Zips the company and marks it as readonly.
     *
     * @return zipped company
     */
    @GET
    @Path("/clone")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response zip() {
        ReplicaSlave replicaSlave = new ReplicaSlave();
        try {
            Response zip = replicaSlave.zip();
            return Response.status(200).entity(zip).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return Response.status(500).build();
    }

    /**
     * Handles the file upload to this company.
     *
     * @param inputStream data of the file
     * @param segments    path of the file
     * @return response
     */
    @POST
    @Path("/file/{any: .*}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response upload(InputStream inputStream, @PathParam("any") List<PathSegment> segments) {
        ReplicaSlaveModel replicaSlaveModel = new ReplicaSlaveModel();
        try {
            String relativePath = PathSegmentUtils.combine(segments);
            replicaSlaveModel.upload(inputStream, relativePath);
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
        ReplicaSlaveModel replicaSlaveModel = new ReplicaSlaveModel();
        try {
            String relativePath = PathSegmentUtils.combine(segments);
            java.nio.file.Path relPath = Paths.get(relativePath);
            byte[] fd = replicaSlaveModel.download(relPath);
            String companyId = replicaSlaveModel.getCompany();
            logger.log(Level.INFO, "Preparing file: " + companyId + "/" + relativePath + " for download");
            if (fd != null) {
                FileResponse fileResponse = new FileResponse(fd);
                logger.log(Level.INFO, "Downloading File: " + companyId + "/" + relativePath);
                return Response.status(200).entity(fileResponse).type(Files.probeContentType(relPath)).build();
            }
            logger.log(Level.WARNING, "File: " + companyId + "/" + relativePath + " not found");
            return Response.status(404).build();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return Response.status(500).build();
    }
}
