package eu.smesec.core.threading;

public final class ThreadFactory {
  private ThreadFactory() {}

  /**
   * <p>Creates a new timer.</p>
   *
   * @param name The name of the timer.
   * @param interval The interval of the timer in milliseconds.
   * @return <code>Timer</code>
   */
  public static Timer createTimer(String name, long interval) {
    return new Timer(name, interval);
  }

  /**
   * <p>Creates a new FileWatcher.</p>
   *
   * @return <code>FileWatcher</code>
   */
  public static FileWatcher createFileWatcher() {
    return new FileWatcher();
  }
}
