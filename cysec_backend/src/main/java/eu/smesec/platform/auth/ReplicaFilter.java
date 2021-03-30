package eu.smesec.platform.auth;

import eu.smesec.platform.auth.strategies.AdminAuthStrategy;
import eu.smesec.platform.auth.strategies.DummyAuthStrategy;
import eu.smesec.platform.auth.strategies.ReplicaAuthStrategy;
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

@Provider
@SecuredReplica
public class ReplicaFilter extends AbstractFilter implements ContainerRequestFilter {
  @Inject private CacheAbstractionLayer cal;
  @Context private ServletContext context;
  @Context private ResourceInfo resourceInfo;

  /** Setup authentication strategies. */
  @PostConstruct
  // use post construct for access on injected dependencies
  public void setup() {
    Config config = CysecConfig.getDefault();
    authStrategies.add(new ReplicaAuthStrategy(cal, config, context));
    authStrategies.add(new AdminAuthStrategy(cal, config, context));
    authStrategies.add(new DummyAuthStrategy(cal, config, context));
  }

  /**
   * Checks replication credentials.
   *
   * @param requestContext The context of the request
   */
  @Override
  public void filter(ContainerRequestContext requestContext) {
    logger.info("Checking for replica authentication");
    Method method = resourceInfo.getResourceMethod();
    checkReqest(requestContext, method);
  }
}
