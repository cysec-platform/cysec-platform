package eu.smesec.platform.auth.strategies;

import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;

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
}
