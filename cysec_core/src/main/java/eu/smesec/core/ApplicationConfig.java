package eu.smesec.core;

import eu.smesec.core.config.Config;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.platform.services.MailServiceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.jsp.JspMvcFeature;

/** Created by Mark Zeman on 14/06/2017. */
public class ApplicationConfig extends ResourceConfig {
  public static final String CONFIG_LOG = "cysec_log_path";
  public static final String BASE_PATH = "cysec_base_path";
  public static final String JSP_TEMPLATE_HOME = "/WEB-INF/classes/templates";

  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  /**
   * Jersey Config constructor.
   *
   * @param context The servlet context
   */
  public ApplicationConfig(final @Context ServletContext context) {
    String contextName = context.getContextPath().substring(1);
    packages("eu.smesec.platform");
    try {
      Config config = CysecConfig.getDefault();
      String basePath = config.getStringValue(contextName, BASE_PATH);
      // check base directory
      if (!new File(basePath).exists()) {
        logger.log(
            Level.SEVERE,
            "The configured cysec base directory "
                + BASE_PATH
                + "=\""
                + basePath
                + "\" for context "
                + contextName
                + " does not exist!"
                + "\n the system will continue to start but will have no usable content!");
      }

      // if log dir does not start with a slash consider it as a relative path to the base dir
      String configLogPath =
          config.getStringValue(contextName, CONFIG_LOG).startsWith("/")
              ? config.getStringValue(contextName, CONFIG_LOG)
              : Paths.get(basePath, config.getStringValue(contextName, CONFIG_LOG)).toString();

      attachLogFileHandler(configLogPath, contextName);

      register(MailServiceImpl.class);
      register(JspMvcFeature.class);
      property(JspMvcFeature.TEMPLATE_BASE_PATH, JSP_TEMPLATE_HOME);
      register(new CysecBinder(context));

      // Todo test remote
      logger.log(Level.INFO, "Checking remote hosts");

    } catch (Exception e) {
      logger.log(Level.WARNING, e, () -> "Problem when constructing " + ApplicationConfig.class.getSimpleName());
    }
  }

  private void attachLogFileHandler(String logDirectory, String context)
      throws IOException {

    // create log directory if it does not exist
    final Path logPath = Paths.get(logDirectory);
    if (Files.notExists(logPath)) {
        logger.log(Level.INFO, "Log directory '" + logPath+ "' does not exist and will be created");
        Files.createDirectories(logPath);
    }

    // create log file if it does not exist
    final Path contextLogfilePath = logPath.resolve(context);
    if (Files.notExists(contextLogfilePath)) {
      try {
        Files.createFile(contextLogfilePath);
      } catch (FileAlreadyExistsException e) {
        // ignored since log file already exists
      }
    }
    logger.log(Level.INFO, "Using log file '" + contextLogfilePath + "'");

    // Configure logger
    FileHandler handler = new FileHandler(contextLogfilePath.normalize().toString());
    handler.setLevel(Level.INFO);
    handler.setFormatter(new SimpleFormatter());
    logger.addHandler(handler);
    register(
        new LoggingFeature(
            logger, Level.INFO, LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));
  }

  @GET
  public Response index() {
    return Response.ok().entity("Top-level").build();
  }
}
