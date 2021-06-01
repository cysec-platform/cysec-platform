package eu.smesec.core.cache;

import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.core.threading.FileWatcher;
import eu.smesec.core.threading.ThreadFactory;
import eu.smesec.core.utils.LocaleUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.jersey.logging.LoggingFeature;

public class CoachManager {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  private final CoachCache cache;
  private final FileWatcher watcher;
  private final Mapper<Questionnaire> mapper;

  /**
   * Coach manager constructor.
   *
   * @param cache Coache cache object
   */
  public CoachManager(CoachCache cache) {
    this.mapper = CacheFactory.createMapper(Questionnaire.class);
    this.cache = cache;
    this.watcher = ThreadFactory.createFileWatcher();
  }

  /**
   * Traverses through the coach directory and registers all coach to the coach cache and observers
   * each directory. The watching service is started after traversing.
   */
  public void init() {
    // init current directory
    watchDirectory(this.cache.getPath());
    visitCoachDirs(
        this.cache.getPath(),
        path -> {
          if (Files.isDirectory(path)) {
            watchDirectory(path);
          } else if (path.toString().endsWith(".xml")) {
            addCoach(path);
          }
        });
    // register watch actions
    watcher.registerOnCreate(
        path -> {
          if (Files.isDirectory(path)) {
            watchDirectory(path);
          } else if (path.toString().endsWith(".xml")) {
            addCoach(path);
          }
        });
    watcher.registerOnModify(
        path -> {
          if (!Files.isDirectory(path) && path.toString().endsWith(".xml")) {
            addCoach(path);
          }
        });
    watcher.registerOnDelete(
        path -> {
          // directory are canceled automatically
          if (path.toString().endsWith(".xml")) {
            this.cache.removeCoach(path);
          }
        });
    watcher.start();
  }

  private void visitCoachDirs(Path path, Consumer<Path> command) {
    /*
    use List<Path> for try-with-resource to close directory stream automatically
    and prevent DirectoryNotEmptyException
     */
    List<Path> paths = new ArrayList<>();
    try (Stream<Path> stream = Files.list(path)) {
      paths = stream.collect(Collectors.toList());
    } catch (IOException ioe) {
      logger.log(
          Level.WARNING,
          "Error during listing files in directory " + path.toString() + ": " + ioe.getMessage());
    }
    for (Path path1 : paths) {
      command.accept(path1);
      if (Files.isDirectory(path1)) {
        visitCoachDirs(path1, command);
      }
    }
  }

  private void watchDirectory(Path path) {
    try {
      watcher.register(path);
      logger.log(Level.INFO, "Observing directory: " + path.toString());
    } catch (IOException e) {
      logger.log(
          Level.WARNING, "Failed observing directory: " + path.toString() + ": " + e.getMessage());
    }
  }

  private void addCoach(Path path) {
    try {
      // wait for file to become available
      Thread.sleep(200);
      Questionnaire coach = mapper.unmarshal(path);
      String coachId = coach.getId();
      String language = coach.getLanguage();
      String parentId = coach.getParent();
      // validate identifiers
      if (coachId == null || coachId.isEmpty()) {
        throw new IllegalArgumentException("CoachId is null or empty");
      }
      if (parentId != null && parentId.isEmpty()) {
        throw new IllegalArgumentException("ParentId is empty");
      }
      if (language != null && !LocaleUtils.isLanguage(language)) {
        throw new IllegalArgumentException("Language tag is invalid: " + language);
      }
      this.cache.addCoach(coachId, parentId, language, path);
    } catch (Exception e) {
      logger.log(Level.WARNING, "Skipping file " + path.toString() + ": " + e.getMessage());
    }
  }

  public CoachCache getCache() {
    return cache;
  }
}
