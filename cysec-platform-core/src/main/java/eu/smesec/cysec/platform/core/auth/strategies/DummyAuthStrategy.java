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
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.core.auth.CryptPasswordStorage;
import eu.smesec.cysec.platform.core.cache.CacheAbstractionLayer;
import eu.smesec.cysec.platform.core.config.Config;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>Provides an authenticator returning always a valid dummy user.</p>
 */
public class DummyAuthStrategy extends AbstractUserAuthStrategy {
  public static final String AUTH_SCHEME = "cysec_authentication_scheme";

  private static final String COMPANY = "acme";
  private static final String USER = "anonymous";

  /**
   * <p>Creates an instance where no authentication is needed.</p>
   *
   * @param cal     the cache layer to read and write company data
   * @param config  the configuration to query
   * @param context the servlet context of the application
   */
  public DummyAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    // create a proxied authenticator (no password verification in the application
    super(cal, config, context, true);
  }

  @Override
  public List<String> getHeaderNames() {
    return new Vector<>();
  }

  @Override
  protected String[] extractCredentials(MultivaluedMap<String, String> header)
      throws CacheException, ClientErrorException {
    // check if the authentication scheme is "dummy"
    boolean dummy = "dummy"
        .equalsIgnoreCase(
            config.getStringValue(context.getContextPath().substring(1), AUTH_SCHEME)
        );
    if (dummy) {
      try {

        // create a new user with the predefined username and a random stron password
        User user = new User();
        user.setUsername(USER);
        CryptPasswordStorage pws = new CryptPasswordStorage(
            CryptPasswordStorage.getRandomHexString(32), null
        );
        user.setPassword(pws.getPasswordStorage());

        // Setup user and company store
        setupCompany(user, COMPANY);

        // return the extracted credentials
        return new String[]{COMPANY, user.getUsername(), null, null};
      } catch (NoSuchAlgorithmException nsae) {
        logger.log(Level.SEVERE, "serious coding problem (THIS SHOULD NOT HAPPEN)", nsae);
        return null;
      }
    } else {

      // do not do authentication if the scheme does not match
      return null;
    }
  }

}
