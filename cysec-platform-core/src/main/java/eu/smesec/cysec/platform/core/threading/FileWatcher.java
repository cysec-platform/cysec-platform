package eu.smesec.cysec.platform.core.threading;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.logging.LoggingFeature;

/**
 * File Watcher Service implementation. Each instance contains it's own WatchService. Multiple
 * directories can be registered or unregistered at runtime.
 */
public class FileWatcher implements IExecutable {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private final AtomicBoolean cont;
  private WatchService watcher;
  private Map<WatchKey, Path> directories;
  private List<IWatcher> onCreate;
  private List<IWatcher> onModify;
  private List<IWatcher> onDelete;

  FileWatcher() {
    this.cont = new AtomicBoolean();
    try {
      this.watcher = FileSystems.getDefault().newWatchService();
      this.directories = new HashMap<>();
      this.onCreate = Collections.synchronizedList(new ArrayList<>());
      this.onModify = Collections.synchronizedList(new ArrayList<>());
      this.onDelete = Collections.synchronizedList(new ArrayList<>());
    } catch (Exception e) {
      logger.log(Level.WARNING, e, () -> "Problem when constructing " + FileWatcher.class.getSimpleName());
    }
  }

  /**
   * Registers a new Directory to this FileWatcher. The directory will be observed after the
   * registration.
   *
   * @param directory The directory to observe.
   * @throws IOException If the directory could not be registered
   */
  public synchronized void register(Path directory) throws IOException {
    WatchKey key = directory.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
    this.directories.put(key, directory);
  }

  /**
   * Unregisters a new Directory to this FileWatcher. The directory will not longer be observed
   * after the deregistration.
   *
   * @param directory The directory to stop observe.
   */
  public synchronized void unregister(Path directory) {
    // use bi directional map instead of iterator
    WatchKey key = null;
    Iterator<Map.Entry<WatchKey, Path>> it = this.directories.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<WatchKey, Path> entry = it.next();
      if (entry.getValue().equals(directory)) {
        key = entry.getKey();
        break;
      }
    }
    if (key != null) {
      key.cancel();
      this.directories.remove(key);
    }
  }

  /**
   * Registers a new action for the create event.
   *
   * @param watcher The action to execute.
   */
  public void registerOnCreate(IWatcher watcher) {
    onCreate.add(watcher);
  }

  /**
   * Registers a new action for the modify event.
   *
   * @param watcher The action to execute.
   */
  public void registerOnModify(IWatcher watcher) {
    onModify.add(watcher);
  }

  /**
   * Registers a new action for the delete event.
   *
   * @param watcher The action to execute.
   */
  public void registerOnDelete(IWatcher watcher) {
    onDelete.add(watcher);
  }

  /** Starts the file watcher thread. */
  @Override
  @SuppressWarnings("unchecked")
  public void start() {
    this.cont.set(true);
    ThreadManager.getInstance()
        .register(
            () -> {
              try {
                logger.log(Level.INFO, "Started file watcher thread.");
                while (!Thread.currentThread().isInterrupted() && this.cont.get()) {
                  WatchKey key = this.watcher.take();
                  Path origin = this.directories.get(key);
                  // Note: Creating a file invokes both a CREATE and MODIFY event.
                  // This is tolerable as duplicates wont be loaded into cache.
                  for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == ENTRY_CREATE) {
                      WatchEvent<Path> ev = (WatchEvent<Path>) event;
                      Path path = origin.resolve(ev.context());
                      logger.log(Level.INFO, "Detected new item: " + path);
                      for (IWatcher watchCreate : onCreate) {
                        try {
                          watchCreate.invoke(path);
                        } catch (IOException e) {
                          logger.log(
                              Level.WARNING,
                              "Error during executing onCreate listener "
                                  + watchCreate.toString()
                                  + ": "
                                  + e.getMessage());
                        }
                      }
                    } else if (kind == ENTRY_MODIFY) {
                      WatchEvent<Path> ev = (WatchEvent<Path>) event;
                      Path path = origin.resolve(ev.context());
                      logger.log(Level.INFO, "Detected modified item: " + path);
                      for (IWatcher watchModify : onModify) {
                        try {
                          watchModify.invoke(path);
                        } catch (IOException e) {
                          logger.log(
                              Level.WARNING,
                              "Error during executing onModify listener "
                                  + watchModify.toString()
                                  + ": "
                                  + e.getMessage());
                        }
                      }
                    } else if (kind == ENTRY_DELETE) {
                      WatchEvent<Path> ev = (WatchEvent<Path>) event;
                      Path path = origin.resolve(ev.context());
                      logger.log(Level.INFO, "Detected removed item: " + path);
                      for (IWatcher watchDelete : onDelete) {
                        try {
                          watchDelete.invoke(path);
                        } catch (IOException e) {
                          logger.log(
                              Level.WARNING,
                              "Error during invoking "
                                  + path.toString()
                                  + " onDelete listener "
                                  + watchDelete.toString()
                                  + ": "
                                  + e.getMessage());
                        }
                      }
                    }
                  }
                  boolean valid = key.reset();
                  if (!valid) {
                    key.cancel();
                    logger.log(Level.INFO, "Canceling item: " + origin.toString());
                  }
                }
                logger.log(Level.INFO, "Stopped file watcher thread");
              } catch (InterruptedException ie) {
                String errorMsg = ie.getMessage();
                logger.log(
                    Level.WARNING,
                    "Interrupted file watcher thread"
                        + (errorMsg != null ? ":" + ie.getMessage() : "")
                        + ".");
              }
            });
  }

  /** Stops the file watcher service. */
  @Override
  public void stop() {
    cont.set(false);
  }
}
