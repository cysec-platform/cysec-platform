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
package eu.smesec.cysec.platform.core;

import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.config.CysecConfig;
import eu.smesec.cysec.platform.core.threading.ThreadManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class CysecServletContextListener implements ServletContextListener {

  // configuration keys referenced in 'cysec.cfgresources'
  private static final String CONFIG_HEADER_PROFILE = "cysec_header_profile";
  private static final String CONFIG_HEADER_LOGOUT = "cysec_header_logout";

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
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ThreadManager.getInstance().shutdown();
  }
}
