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
package eu.smesec.cysec.platform.core.auth.strategies;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.generated.Locks;
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.logging.LoggingFeature;

public abstract class AbstractAuthStrategy {
  protected Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  protected CacheAbstractionLayer cal;
  protected Config config;
  protected ServletContext context;
  private final boolean proxyAuth;

  /**
   * Auth strategy base class.
   *
   * @param cal The cache abstraction layer
   * @param config The platform configuration
   * @param context The servlet context
   * @param proxyAuth platform has a proxy
   */
  public AbstractAuthStrategy(
      CacheAbstractionLayer cal, Config config, ServletContext context, boolean proxyAuth) {
    this.cal = cal;
    this.config = config;
    this.context = context;
    this.proxyAuth = proxyAuth;
  }

  public boolean isProxyAuth() {
    return proxyAuth;
  }

  public abstract List<String> getHeaderNames();

  public abstract boolean authenticate(MultivaluedMap<String, String> headers, Method method)
      throws CacheException, ClientErrorException;

  /**
   * Verifies if either company or user are present. If the company is not present a new company and
   * user are created. If only the user is missing, a new user is created.
   *
   * @param user The user logging in.
   * @param companyId The users company
   */
  void setupCompany(final User user, final String companyId) {
    logger.info("Checking oauth credentials");
    try {
      // Check if company exists...
      if (cal.existsCompany(companyId)) {
        // ... and create user if no duplicate is found
        User duplicate = cal.getUserByName(companyId, user.getUsername());
        if (duplicate == null) {
          logger.info(
              String.format(
                  "Creating new user as %s not present in company %s.",
                  user.getUsername(), companyId));
          user.setLock(Locks.NONE);
          cal.createUser(companyId, user);
        } else {
          logger.info("Found existing user: " + user.getUsername());
        }
      } else {
        logger.info(
            String.format(
                "Creating first user %s in new company %s.", user.getUsername(), companyId));

        // first user of a new company has to be admin
        user.getRole().add("Admin");
        user.setLock(Locks.NONE);
        cal.createCompany(companyId, companyId, user);
      }
      logger.info(
          String.format(
              "Returning verified Header data: user %s, company %s, email %s",
              user.getUsername(), companyId, user.getEmail()));
    } catch (CacheException ce) {
      logger.warning(ce.getMessage());
    }
  }

}
