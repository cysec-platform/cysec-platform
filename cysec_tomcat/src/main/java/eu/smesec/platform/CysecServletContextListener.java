package eu.smesec.platform;

import eu.smesec.core.config.Config;
import eu.smesec.core.config.CysecConfig;
import eu.smesec.core.threading.ThreadManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class CysecServletContextListener implements ServletContextListener {

  // configuration keys referenced in 'cysec.cfgresources'
  private static final String CONFIG_HEADER_PROFILE = "cysec_header_profile";
  private static final String CONFIG_HEADER_LOGOUT = "cysec_header_logout";

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    final ServletContext servletContext = servletContextEvent.getServletContext();
    final Config config = CysecConfig.getDefault();

    final String headerProfileHref = config.getStringValue(null, CONFIG_HEADER_PROFILE);
    if (headerProfileHref != null) {
      servletContext.setInitParameter("header_profile_href", headerProfileHref);
    }

    final String headerLogoutHref = config.getStringValue(null, CONFIG_HEADER_LOGOUT);
    if (headerLogoutHref != null) {
      servletContext.setInitParameter("header_logout_href", headerLogoutHref);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ThreadManager.getInstance().shutdown();
  }
}
