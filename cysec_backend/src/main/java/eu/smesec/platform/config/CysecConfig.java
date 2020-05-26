package eu.smesec.platform.config;

import java.util.ArrayList;
import java.util.List;
import org.glassfish.jersey.logging.LoggingFeature;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CysecConfig extends Config {
    private static Logger LOGGER = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
    public static final String CONFIG_FOLDER = "etc";
    public static final String CONFIG_FILE = "cysec.conf";
    public static final String CONFIG_RES = "cysec.cfgresource";
    public static String BASE_PATH = "";
    public static String RESOURCE_FOLDER = "etc/";
    private static final String RESOURCE_FILE = "cysec.cfgresources";


    private static Config config = null;
    private static Object monitor = new Object();

    private CysecConfig() throws IOException {
        // This constructor hides a default constructor
        super(RESOURCE_FOLDER + "/" + RESOURCE_FILE);
        BASE_PATH = initBasePath().toString();
        LOGGER.info(String.format("Loading config file from base \"%s\"", BASE_PATH));
        load(BASE_PATH + "/" + CONFIG_FOLDER + "/" + CONFIG_FILE);
    }

    public static Config getDefault() {
        synchronized (monitor) {
            if (config == null) {
                try {
                    config = new CysecConfig();
                } catch (IOException ioe) {
                    throw new RuntimeException("Error while opening config file", ioe);
                }
            }
        }
        return config;
    }

    private static Path initBasePath() {
        // read from /var/lib
        // read from current dir ./ (DEV location)

        List<String> candidates = new ArrayList<>(4);
        candidates.add("/var/lib/cysec/");
        candidates.add("/etc/cysec/");
        candidates.add("./"); //TODO discuss
        String env = System.getenv("CYSEC_HOME");
        if (env != null) {
            candidates.add(env); //TODO discuss
        }

        Path finalPath = null;
        for (String pathCandidate : candidates) {
            Path p = Paths.get(pathCandidate, CONFIG_FOLDER, CONFIG_FILE);
            if (Files.exists(p)) {
                LOGGER.info(String.format("Found configuration path in \"%s\"", pathCandidate));
                finalPath = Paths.get(pathCandidate);
                break;
            } else {
                LOGGER.info(String.format("Config file not found in \"%s\"", p));
            }
        }
        if (finalPath != null) {

            // todo doing selfcheck
            for (String i : new String[]{"standard login", "authentication filter config", "access to external M2M API"}) {
                LOGGER.log(Level.INFO, "Checking " + i);
                try {
                    Thread.sleep((long) (Math.random() * 2000));
                } catch (Exception e) {
                }
                LOGGER.log(Level.INFO, i + " checked successfully");
            }

            return finalPath;
        } else {
            LOGGER.warning(String.format("No configuration file found (thats really bad... consider making an empty file named ./etc/cysec.conf)"));
            return null;
        }
    }

}
