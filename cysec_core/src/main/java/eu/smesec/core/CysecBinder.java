package eu.smesec.core;

import eu.smesec.core.cache.CacheAbstractionLayer;
import eu.smesec.core.cache.ResourceManager;
import eu.smesec.core.config.Config;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.platform.services.MailServiceImpl;

import java.nio.file.Paths;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class CysecBinder extends AbstractBinder {
  private ServletContext context;

  public CysecBinder(@Context ServletContext context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    try {
      Config config = CysecConfig.getDefault();
      // Instantiate Singletons
      ResourceManager resManager =
          new ResourceManager(Paths.get(context.getRealPath(ApplicationConfig.JSP_TEMPLATE_HOME)));
      CacheAbstractionLayer cal = new CacheAbstractionLayer(context, resManager, config);
      MailServiceImpl mailService = new MailServiceImpl();
      // bind Singletons
      bind(cal).to(CacheAbstractionLayer.class);
      bind(resManager).to(ResourceManager.class);
      bind(mailService).to(MailServiceImpl.class);
    } catch (Exception e) {
      throw new RuntimeException("Error in binding configuration");
    }
  }
}
