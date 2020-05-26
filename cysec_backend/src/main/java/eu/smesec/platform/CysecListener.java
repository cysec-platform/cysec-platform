package eu.smesec.platform;

import eu.smesec.platform.threading.ThreadManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class CysecListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {

  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ThreadManager.getInstance().shutdown();
  }
}
