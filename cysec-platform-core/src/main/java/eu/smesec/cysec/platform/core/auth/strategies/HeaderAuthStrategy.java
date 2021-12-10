/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Provides a proxied auth strategy where all user information is extracted from header
 * lines set by a proxy.
 */
public final class HeaderAuthStrategy extends AbstractUserAuthStrategy {
  /* The OIDC parameters are the headers that contain user info from keycloak. Even though */
  public static final String OIDC_NAME = "cysec_header_username";

  public static final String OIDC_MAIL = "cysec_header_email";
  public static final String OIDC_COMPANY = "cysec_header_company";
  public static final String OIDC_FIRSTNAME = "cysec_header_firstname";
  public static final String OIDC_LASTNAME = "cysec_header_lastname";
  public static final String OIDC_LOCALE = "cysec_header_locale";

  private final List<String> headerNames;

  /**
   * Header authentication strategy constructor.
   *
   * @param cal     The cache abstraction layer
   * @param config  The platform configuration
   * @param context The servlet context
   */
  public HeaderAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, true);
    String contextName = context.getContextPath().substring(1);
    headerNames =
        new ArrayList<>(
            Arrays.asList(
                config.getStringValue(contextName, OIDC_NAME),
                config.getStringValue(contextName, OIDC_MAIL),
                config.getStringValue(contextName, OIDC_COMPANY),
                config.getStringValue(contextName, OIDC_FIRSTNAME),
                config.getStringValue(contextName, OIDC_LASTNAME),
                config.getStringValue(contextName, OIDC_LOCALE)));
  }

  @Override
  public List<String> getHeaderNames() {
    return headerNames.subList(0, 3);
  }

  @Override
  protected String[] extractCredentials(MultivaluedMap<String, String> header)
      throws CacheException, ClientErrorException {
    logger.info("Checking Header auth");
    // verify essential headers
    String oauthName = requireNonNull(headerNames.get(0), header.getFirst(headerNames.get(0)));
    String oauthMail = requireNonNull(headerNames.get(1), header.getFirst(headerNames.get(1)));

    String oauthFirstName = header.getFirst(headerNames.get(3));
    String oauthLastName = header.getFirst(headerNames.get(4));
    String oauthLocale = header.getFirst(headerNames.get(5));

    // Prepare user object to create new domain user if necessary
    User user = new User();
    user.setUsername(oauthName);
    user.setFirstname(oauthFirstName);
    user.setLocale(oauthLocale);
    user.setEmail(oauthMail);

    String oauthCompany = requireNonNull(headerNames.get(2), header.getFirst(headerNames.get(2)));
    setupCompany(user, oauthCompany);

    return new String[]{oauthCompany, oauthName, null, oauthLocale};
  }

  private static String requireNonNull(String field, String value) {
    if (value == null || value.isEmpty()) {
      throw new BadRequestException("missing oidc fields " + field);
    }
    return value;
  }

}
