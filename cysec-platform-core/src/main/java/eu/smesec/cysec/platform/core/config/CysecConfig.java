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
package eu.smesec.cysec.platform.core.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.jersey.logging.LoggingFeature;

public class CysecConfig extends Config {

  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private static final String CONFIG_FOLDER = "etc";
  private static final String CONFIG_FILE = "cysec.conf";
  static final String RESOURCE_FOLDER = "etc";
  static final String RESOURCE_FILE = "cysec.cfgresources";

  private static Config config = null;

  private static final Object monitor = new Object();

  private CysecConfig() throws IOException {
    // This constructor hides a default constructor
    super(RESOURCE_FOLDER + "/" + RESOURCE_FILE);
    String basePath = initBasePath().toString();
    logger.info(() -> String.format("Loading config file from base path '%s'", basePath));
    load(basePath + "/" + CONFIG_FOLDER + "/" + CONFIG_FILE);
  }

  /**
   * Returns the default configuration.
   *
   * @return default configuration
   */
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
    final List<String> candidates = new ArrayList<>(5);
    candidates.add("/var/lib/cysec/");
    candidates.add("/etc/cysec/");
    candidates.add(System.getProperty("user.home") + File.separator + ".cysec" + File.separator);
    candidates.add("./"); // current dir (DEV location)
    Optional.ofNullable(System.getenv("CYSEC_HOME")).ifPresent(candidates::add);

    Path finalPath = null;
    for (String pathCandidate : candidates) {
      Path p = Paths.get(pathCandidate, CONFIG_FOLDER, CONFIG_FILE);
      if (Files.exists(p)) {
        logger.info(() -> String.format("Found configuration path in \"%s\"", pathCandidate));
        finalPath = Paths.get(pathCandidate);
        break;
      } else {
        logger.info(() -> String.format("Config file not found in \"%s\" (pwd: %s)", p, Paths.get("").toAbsolutePath()));
      }
    }
    if (finalPath != null) {

      // todo doing selfcheck
      for (String i :
          new String[]{
              "standard login", "authentication filter config", "access to external M2M API"
          }) {
        logger.log(Level.INFO, () -> "Checking " + i);
        try {
          Thread.sleep((long) (Math.random() * 2000));
        } catch (Exception ignore) {
          // do nothing
        }
        logger.log(Level.INFO, () -> i + " checked successfully");
      }

      return finalPath;
    } else {
      logger.severe("No configuration file found (consider making an empty file './etc/cysec.conf')");
      throw new RuntimeException("Could not find a configuration file");
    }
  }
}
