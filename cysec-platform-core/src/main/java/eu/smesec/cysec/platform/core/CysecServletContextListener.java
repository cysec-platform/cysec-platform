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
package eu.smesec.cysec.platform.core;

import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.config.CysecConfig;
import eu.smesec.cysec.platform.core.threading.ThreadManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.glassfish.jersey.logging.LoggingFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import java.util.logging.Logger;;

@WebListener
public class CysecServletContextListener implements ServletContextListener {
  protected static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  // configuration keys referenced in 'cysec.cfgresources'
  private static final String CONFIG_HEADER_PROFILE = "cysec_header_profile";
  private static final String CONFIG_HEADER_LOGOUT = "cysec_header_logout";
  private static final String CONFIG_LOGO_PATH = "cysec_logo_path";

  private static final String LOGO_ASSET_PATH = "/assets/logo/logo.svg";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    final ServletContext servletContext = servletContextEvent.getServletContext();
    final Config config = CysecConfig.getDefault();

    final String headerProfileHref = config.getStringValue(null, CONFIG_HEADER_PROFILE);
    if (headerProfileHref != null) {
      servletContext.setInitParameter("header_profile_href", headerProfileHref);
    }

    final String headerLogoutHref = config.getStringValue(null, CONFIG_HEADER_LOGOUT);
    if (headerLogoutHref != null) {
      servletContext.setInitParameter("header_logout_href", headerLogoutHref);
    }

    final String logoPath = config.getStringValue(null, CONFIG_LOGO_PATH);
    if (logoPath != null) {
      // if available, overwrite logo.svg with a custom logo file

      Path source = Path.of(logoPath);
      String defaultLogo = servletContext.getRealPath(LOGO_ASSET_PATH);
      Path target = Path.of(defaultLogo);

      try {
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        logger.info("replaced default logo with custom logo");
      } catch (IOException e) {
        logger.severe("could not copy custom logo");
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ThreadManager.getInstance().shutdown();
  }
}
