package eu.smesec.platform.auth.strategies;

import static eu.smesec.platform.auth.strategies.BasicAuthStrategy.AUTHORIZATION_PROPERTY;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.generated.User;
import eu.smesec.platform.auth.CryptPasswordStorage;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import javax.servlet.ServletContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

public class DummyAuthStrategy extends AbstractUserAuthStrategy {
  public static final String AUTH_SCHEME = "cysec_authentication_scheme";

  private final List<String> headers;

  private final String COMPANY = "acme";
  private final String USER = "anonymous";

  public DummyAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, true);
    this.headers = Collections.singletonList(AUTHORIZATION_PROPERTY);
  }

  @Override
  public List<String> getHeaderNames() {
    return new Vector<>();
  }

  @Override
  protected String[] extractCredentials(MultivaluedMap<String, String> header) throws CacheException, ClientErrorException {
    boolean dummy = "dummy".equals(config.getStringValue(context.getContextPath().substring(1), AUTH_SCHEME).toLowerCase());
    if (dummy) {
      try {
        User user = new User();
        user.setUsername(USER);
        CryptPasswordStorage pws = new CryptPasswordStorage(CryptPasswordStorage.getRandomHexString(32), null);
        user.setPassword(pws.getPasswordStorage());
        setupCompany(user, COMPANY);
        return new String[]{COMPANY, user.getUsername(), null, null};
      } catch (NoSuchAlgorithmException nsae) {
        logger.log(Level.SEVERE, "serious coding problem (THIS SHOULD NOT HAPPEN)", nsae);
        return null;
      }
    } else {
      return null;
    }
  }

}
