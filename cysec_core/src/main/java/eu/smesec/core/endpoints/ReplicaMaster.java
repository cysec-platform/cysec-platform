package eu.smesec.core.endpoints;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.ConfigException;
import eu.smesec.core.auth.Secured;
import eu.smesec.core.auth.SecuredAdmin;
import eu.smesec.core.auth.strategies.ReplicaAuthStrategy;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.config.Config;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.core.utils.FileResponse;
import eu.smesec.core.utils.PathSegmentUtils;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;

@Path("replica/master")
public class ReplicaMaster {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  public static final String REPLICA_HOST = "cysec_replica_host";
  public static final String REPLICA_TOKEN = "cysec_replica_token";

  @Context private ServletContext context;
  @Inject private CacheAbstractionLayer cal;

  private Client client;
  private Config config;

  /**
   * Initialization after construction.
   */
  @PostConstruct
  public void init() {
    this.client = ClientBuilder.newClient();
    this.config = CysecConfig.getDefault();
  }

  /**
   * Clones a company from the slave into this master.
   * The cloned company inside the slave will be marked as readonly.
   *
   * @param companyId The id of the company to clone, replica token must match
   * @return response
   */
  @SecuredAdmin
  @POST
  @Path("/clone/{id}")
  public Response cloneCompany(@PathParam("id") String companyId) {
    String contextName = context.getContextPath().substring(1);
    try {
      if (companyId == null || companyId.isEmpty()) {
        logger.log(Level.WARNING, "Invalid company id");
        return Response.status(400).build();
      }
      String remote = getReplicaEntry(contextName, REPLICA_HOST);
      String companyToken = getCompanyToken(contextName, companyId);
      logger.log(Level.INFO, "Downloading company " + companyId + " from " + remote);
      Response res =
          client
              .target(remote + "/api/replica/clone/")
              .request(MediaType.APPLICATION_OCTET_STREAM)
              .header(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, companyToken)
              .get();
      if (res.getStatus() == 200) {
        logger.log(Level.INFO, "Successfully downloaded company " + companyId + " from " + remote);
        InputStream is = res.readEntity(InputStream.class);
        logger.log(Level.INFO, "Installing company " + companyId);
        cal.createCompanyFromArchive(companyId, is);
        logger.log(Level.INFO, "Successfully installed company " + companyId);
        return Response.status(204).build();
      }
      logger.log(Level.WARNING, "Failed downloading company " + companyId);
      return Response.status(400).build();
    } catch (ProcessingException pe) {
      logger.log(Level.WARNING, "Error during downloading company " + companyId, pe);
    } catch (ConfigException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Pushes the changed file to the slave and synchronizes them.
   *
   * @param segments file path, relative to the company directory
   * @return response
   */
  @Secured
  @POST
  @RolesAllowed("Admin")
  @Path("/push/{any: .*}")
  public Response push(@PathParam("any") List<PathSegment> segments) {
    String companyId = context.getAttribute("company").toString();
    String contextName = context.getContextPath().substring(1);
    String relative = PathSegmentUtils.combine(segments);
    try {
      String remote = getReplicaEntry(contextName, REPLICA_HOST);
      String companyToken = getCompanyToken(contextName, companyId);
      FileResponse fd = cal.createFileResponse(companyId, Paths.get(relative));
      if (fd != null) {
        logger.log(Level.INFO, "Uploading file " + relative + " to " + remote);
        Response res =
            client
                .target(remote + "/api/replica/file/" + relative)
                .request()
                .header(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, companyToken)
                .post(Entity.entity(fd, MediaType.APPLICATION_OCTET_STREAM));
        if (res.getStatus() == 204) {
          logger.log(Level.INFO, "Successfully uploaded file " + relative + " to " + remote);
        } else {
          logger.log(
              Level.WARNING,
              "Failed uploading file"
                  + relative
                  + " to "
                  + remote
                  + ". Server responded with status code "
                  + res.getStatus());
        }
        return res;
      }
      logger.log(Level.WARNING, "File " + relative + " was not found in company " + companyId);
      return Response.status(400).build();
    } catch (ProcessingException pe) {
      logger.log(Level.WARNING, "Error during uploading file " + relative, pe);
      return Response.status(500).build();
    } catch (ConfigException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  /**
   * Pulls changes from the slave into this master and synchronizes them.
   * This method should not be used.
   *
   * @param segments file path, relative to the company directory
   * @return response
   */
  @Secured
  @POST
  @RolesAllowed("Admin")
  @Path("/pull/{any: .*}")
  public Response pull(@PathParam("any") List<PathSegment> segments) {
    String companyId = context.getAttribute("company").toString();
    String contextName = context.getContextPath().substring(1);
    String relative = PathSegmentUtils.combine(segments);
    try {
      String remote = getReplicaEntry(contextName, REPLICA_HOST);
      String companyToken = getCompanyToken(contextName, companyId);
      logger.log(Level.INFO, "Downloading file " + relative + " from " + remote);
      Response res =
          client
              .target(remote + "/api/replica/file/" + relative)
              .request(MediaType.APPLICATION_OCTET_STREAM)
              .header(ReplicaAuthStrategy.REPLICA_TOKEN_HEADER, companyToken)
              .get();
      if (res.getStatus() == 200) {
        logger.log(Level.INFO, "Successfully downloaded file " + relative + " from " + remote);
        InputStream is = res.readEntity(InputStream.class);
        logger.log(Level.INFO, "Synchronizing file " + relative);
        cal.syncFile(companyId, Paths.get(relative), is, true);
        logger.log(Level.INFO, "Successfully synchronized file " + relative);
        return Response.status(204).build();
      }
      logger.log(Level.WARNING, "Failed downloading file " + relative + " from " + remote);
      return res;
    } catch (ProcessingException pe) {
      logger.log(Level.WARNING, "Error during downloading file " + relative, pe);
      return Response.status(500).build();
    } catch (ConfigException | CacheException ce) {
      logger.log(Level.WARNING, ce.getMessage(), ce);
      return Response.status(400).build();
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage(), e);
    }
    return Response.status(500).build();
  }

  private String getCompanyToken(String context, String companyId) throws ConfigException {
    Optional<String> companyToken =
        Arrays.stream(getReplicaEntry(context, REPLICA_TOKEN).split(" "))
            .filter(entry -> entry.startsWith(companyId + "/"))
            .findFirst();
    if (!companyToken.isPresent()) {
      throw new ConfigException("No replica token found for company " + companyId);
    }
    return companyToken.get();
  }

  private String getReplicaEntry(String context, String key) throws ConfigException {
    String entry = config.getStringValue(context, key);
    if (entry == null) {
      throw new ConfigException("Cannot find remote host entry " + key);
    }
    return entry;
  }
}
