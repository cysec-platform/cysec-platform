package eu.smesec.platform.auth;

import eu.smesec.platform.auth.strategies.BasicAuthStrategy;
import eu.smesec.platform.auth.strategies.HeaderAuthStrategy;
import eu.smesec.platform.cache.CacheAbstractionLayer;
import eu.smesec.platform.config.Config;
import eu.smesec.platform.config.CysecConfig;

import java.lang.reflect.Method;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

/**
 * This filter verifies the access permissions for a user based on username and password provided in
 * request.
 */
@Provider
@Secured
public class AuthFilter extends AbstractFilter implements ContainerRequestFilter {
  @Inject private CacheAbstractionLayer cal;
  @Context private ServletContext context;
  @Context private ResourceInfo resourceInfo;

  /**
   * Setup strategies if enabled.
   */
  @PostConstruct
  // use post construct for access on injected dependencies
  public void setup() {
    Config config = CysecConfig.getDefault();
    String contextName = context.getContextPath().substring(1);
    if (!config.getBooleanValue(contextName, "cysec_standalone")) {
      authStrategies.add(new HeaderAuthStrategy(cal, config, context));
    }
    authStrategies.add(new BasicAuthStrategy(cal, config, context));
  }

  /**
   * Checks user credentials.
   * @param requestContext The context of the request
   */
  @Override
  public void filter(ContainerRequestContext requestContext) {
    logger.info("Checking for user authentication");
    Method method = resourceInfo.getResourceMethod();
    checkReqest(requestContext, method);
  }
}
