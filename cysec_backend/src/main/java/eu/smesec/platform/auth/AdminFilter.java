package eu.smesec.platform.auth;

import eu.smesec.platform.auth.strategies.AdminAuthStrategy;
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
 * <p>This filter verifies the access permissions for a user
 * based on username and password provided in request.</p>
 */
@Provider
@SecuredAdmin
public class AdminFilter extends AbstractFilter implements ContainerRequestFilter {

  @Inject
  private CacheAbstractionLayer cal;
  @Context
  private ServletContext context;
  @Context
  private ResourceInfo resourceInfo;

  @PostConstruct
  // use post construct for access on injected dependencies
  public void setup() {
    Config config = CysecConfig.getDefault();
    authStrategies.add(new AdminAuthStrategy(cal, config, context));
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    logger.info("Checking for admin authentication");
    Method method = resourceInfo.getResourceMethod();
    checkReqest(requestContext, method);
  }
}