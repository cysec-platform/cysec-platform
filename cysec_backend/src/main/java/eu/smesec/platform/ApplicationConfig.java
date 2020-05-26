package eu.smesec.platform;

import eu.smesec.platform.config.Config;
import eu.smesec.platform.config.CysecConfig;

import static eu.smesec.platform.config.CysecConfig.*;

import eu.smesec.platform.services.MailServiceImpl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
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

/**
 * Created by Mark Zeman on 14/06/2017.
 */
public class ApplicationConfig extends ResourceConfig {
    public static final String CONFIG_LOG = "cysec_log_path";
    public static final String BASE_PATH = "cysec_base_path";
    public static final String JSP_TEMPLATE_HOME = "/WEB-INF/classes/templates";

    private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    ;

    /***
     * <p>FIXME.</p>
     *
     * @param context FIXME
     */
    public ApplicationConfig(final @Context ServletContext context) {
        String contextName = context.getContextPath().substring(1);
        packages("eu.smesec.platform");
        try {
            Config config = CysecConfig.getDefault();
            String basePath = config.getStringValue(contextName, BASE_PATH);
            // check base directory
            if (!new File(basePath).exists()) {
                logger.log(Level.SEVERE, "The configured cysec base directory " + BASE_PATH + "=\"" + basePath + "\" for context " + contextName + " does not exist!"
                        + "\n the system will continue to start but will have no usable content!");
            }

            // if log dir does not start with a slash consider it as a relative path to the base dir
            String configLogPath = config.getStringValue(contextName, CONFIG_LOG).startsWith("/")
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
            e.printStackTrace();
        }
    }

    private void attachLogFileHandler(String configLogPath, String context)
            throws IOException, URISyntaxException {
        // check log directory
        Path logPath = Paths.get(configLogPath, context);
        /*if(!Files.exists(logPath)) {
            logger.log(Level.INFO, "Log path \"" + logPath+ "\" is missing.. creating directory");
            logPath.toFile().mkdirs();
        }*/
        File contextLogFile = logPath.toFile();
        if (contextLogFile.getParentFile().exists()) {
            contextLogFile.createNewFile();
            logger.log(Level.INFO, "Using log file " + contextLogFile.toString());
        } else {
            // Path newLogPath = null; Paths.get(this.getClass().getResource("/logs/").toURI());
            logger.log(Level.SEVERE, "Path to " + logPath + " did not exist. You will have to look hard for instance logs of " + context);
            //logPath = newLogPath;
            //contextLogFile = new File(logPath.toUri());
        }

        // Configure logger
        FileHandler handler = new FileHandler(contextLogFile.getPath());
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        register(new LoggingFeature(logger, Level.INFO,
                LoggingFeature.Verbosity.HEADERS_ONLY, Integer.MAX_VALUE));
    }

    @GET
    public Response index() {
        return Response.ok().entity("Top-level").build();
    }
}
