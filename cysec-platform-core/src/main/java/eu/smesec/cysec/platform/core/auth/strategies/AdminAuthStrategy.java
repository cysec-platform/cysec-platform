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
package eu.smesec.cysec.platform.core.auth.strategies;

import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;
import eu.smesec.cysec.platform.core.utils.Validator;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.internal.util.Base64;

public class AdminAuthStrategy extends AbstractAuthStrategy {
  public static final String ADMIN_PREFIX = "cysec_admin_prefix";
  public static final String ADMIN_NAMES = "cysec_admin_users";
  public static final String ADMIN_PWS = "cysec_admin_passwords";
  private final List<String> headers;

  public AdminAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, false);
    this.headers = Collections.singletonList(BasicAuthStrategy.AUTHORIZATION_PROPERTY);
  }

  @Override
  public List<String> getHeaderNames() {
    return headers;
  }

  @Override
  public boolean authenticate(MultivaluedMap<String, String> headers, Method method)
      throws CacheException, ClientErrorException {
    logger.info("Checking Admin auth");
    String contextName = context.getContextPath().substring(1);
    String basicAuth = headers.getFirst(BasicAuthStrategy.AUTHORIZATION_PROPERTY);
    if (basicAuth != null && !basicAuth.isEmpty()) {
      Matcher basicMatcher = BasicAuthStrategy.regexBasic.matcher(basicAuth);
      if (basicMatcher.matches()) {
        String auth = Base64.decodeAsString(basicMatcher.group(1));
        Matcher authMatcher = BasicAuthStrategy.regexAuth.matcher(auth);
        if (!authMatcher.matches()) {
          logger.log(Level.WARNING, "invalid auth format: " + auth);
          throw new BadRequestException("invalid auth format: " + auth);
        }
        // check admin prefix against company
        String adminPrefix = config.getStringValue(contextName, ADMIN_PREFIX);
        String companyName = authMatcher.group(1);
        if (!adminPrefix.equalsIgnoreCase(companyName)) {
          logger.log(Level.INFO, "company " + companyName + " is not a server admin prefix");
          return false;
        }
        // get admin username and password
        String username = authMatcher.group(2);
        if (!Validator.validateWord(username)) {
          throw new BadRequestException("Username pattern does not match");
        }
        String password = authMatcher.group(3);
        if (password == null || password.isEmpty()) {
          throw new BadRequestException("Password is null or empty");
        }
        List<String> adminNames =
            Arrays.asList(config.getStringValue(contextName, ADMIN_NAMES).split(" "));
        List<String> admimPws =
            Arrays.asList(config.getStringValue(contextName, ADMIN_PWS).split(" "));
        if (adminNames.size() != admimPws.size()) {
          logger.log(
              Level.WARNING,
              "number of server admin names " + "and number of server passwords is not equals");
        }
        int i = adminNames.indexOf(username);
        if (i < 0) {
          logger.log(Level.WARNING, "server admin name " + username + " is not in admin list");
          return false;
        }
        if (i >= admimPws.size() || !admimPws.get(i).equalsIgnoreCase(password)) {
          logger.log(Level.WARNING, "server admin password " + password + " does not match");
          return false;
        }
        logger.log(Level.INFO, "server admin " + username + " successfully logged in");
        return true;
      }
    }
    logger.log(Level.WARNING, "invalid auth header");
    throw new BadRequestException("invalid auth header");
  }
}
