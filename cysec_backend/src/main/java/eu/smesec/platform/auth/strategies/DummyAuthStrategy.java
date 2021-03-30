package eu.smesec.platform.auth.strategies;

import static eu.smesec.platform.auth.strategies.BasicAuthStrategy.AUTHORIZATION_PROPERTY;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.servlet.ServletContext;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

public class DummyAuthStrategy extends AbstractUserAuthStrategy {
  public static final String AUTH_SCHEME = "cysec_authentication_scheme";

  private final List<String> headers;

  public DummyAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, false);
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
      return new String[]{"acme", "anonymous", "", null};
    } else {
      return null;
    }
  }
}
