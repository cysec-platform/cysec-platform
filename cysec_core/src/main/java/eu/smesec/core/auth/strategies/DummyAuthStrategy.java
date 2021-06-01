package eu.smesec.core.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.User;
import eu.smesec.core.auth.CryptPasswordStorage;
import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.config.Config;
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
