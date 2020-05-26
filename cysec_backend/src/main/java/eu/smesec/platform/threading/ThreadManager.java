package eu.smesec.platform.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Global executor service as singleton
 */
public class ThreadManager {
  private static final ThreadManager tm = new ThreadManager();

  /**
   * <p>Returns the singleton instance.</p>
   *
   * @return instance
   */
  public static ThreadManager getInstance() {
    return tm;
  }

  private final ExecutorService service;

  private ThreadManager() {
    service = Executors.newCachedThreadPool();
  }

  /**
   * <p>Submits a new task to the thread manager.</p>
   *
   * @param task The task to execuete.
   */
  public void register(Runnable task) {
    service.submit(task);
  }

  /**
   * <p>Cancels each task and shuts down the thread manager.</p>
   */
  public void shutdown() {
    service.shutdownNow();
  }
}
