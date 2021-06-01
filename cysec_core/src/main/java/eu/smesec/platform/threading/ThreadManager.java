package eu.smesec.platform.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Global executor service as singleton. */
public class ThreadManager {
  private static final ThreadManager tm = new ThreadManager();

  /**
   * Returns the singleton instance.
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
   * Submits a new task to the thread manager.
   *
   * @param task The task to execute
   */
  public void register(Runnable task) {
    service.submit(task);
  }

  /** Cancels each task and shuts down the thread manager. */
  public void shutdown() {
    service.shutdownNow();
  }
}
