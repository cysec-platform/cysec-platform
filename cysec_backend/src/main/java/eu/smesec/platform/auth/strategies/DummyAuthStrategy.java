package eu.smesec.platform.auth.strategies;

import static eu.smesec.platform.auth.strategies.BasicAuthStrategy.AUTHORIZATION_PROPERTY;
import static eu.smesec.platform.auth.strategies.BasicAuthStrategy.regexAuth;
import static eu.smesec.platform.auth.strategies.BasicAuthStrategy.regexBasic;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;
import eu.smesec.platform.utils.Validator;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.regex.Matcher;
import javax.servlet.ServletContext;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.internal.util.Base64;

public class DummyAuthStrategy extends AbstractUserAuthStrategy {
  public static final String AUTH_SCHEME = "cysec_authentication_scheme";

  private final List<String> headers;

  public DummyAuthStrategy(CacheAbstractionLayer cal, Config config, ServletContext context) {
    super(cal, config, context, false);
    this.headers = Collections.singletonList(AUTHORIZATION_PROPERTY);
  }

  @Override
  public List<String> getHeaderNames() {
    return null;
  }

  @Override
  public boolean authenticate(MultivaluedMap<String, String> headers, Method method)
      throws CacheException, ClientErrorException {
    String contextName = context.getContextPath().substring(1);
    if ("dummy".equals(config.getStringValue(contextName, AUTH_SCHEME).toLowerCase())) {
      logger.info("Checking Dummy auth");
        return true;
    }
    return false;
  }

  @Override
  protected String[] extractCredentials(MultivaluedMap<String, String> header) throws CacheException, ClientErrorException {
    return new String[] {"acme","dummy","dummy", Locale.ENGLISH.toString()};
  }
}
