package eu.smesec.platform;

import eu.smesec.platform.config.Config;
import eu.smesec.platform.config.CysecConfig;
import eu.smesec.platform.threading.ThreadManager;

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
    final Config config = CysecConfig.getDefault();
    final ServletContext servletContext = servletContextEvent.getServletContext();
    servletContext.setInitParameter("header_profile_href", config.getStringValue(null, CONFIG_HEADER_PROFILE));
    servletContext.setInitParameter("header_logout_href", config.getStringValue(null, CONFIG_HEADER_LOGOUT));
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ThreadManager.getInstance().shutdown();
  }
}
