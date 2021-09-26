package eu.smesec.cysec.platform.core.cache;

import eu.smesec.bridge.ILibCal;
import eu.smesec.bridge.CoachLibrary;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.execptions.LibraryException;
import eu.smesec.bridge.execptions.MapperException;
import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.bridge.utils.Tuple;
import eu.smesec.cysec.platform.core.jaxb.FieldCopyStrategy;
import eu.smesec.cysec.platform.core.jaxb.GetterMergeStrategy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.logging.LoggingFeature;
import org.jvnet.jaxb2_commons.lang.CopyStrategy2;
import org.jvnet.jaxb2_commons.lang.MergeStrategy2;
import org.jvnet.jaxb2_commons.locator.DefaultRootObjectLocator;

class CoachCache extends Cache {
  private static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);
  // copy only objects with text fields, used for translations
  private static final CopyStrategy2 fieldCopyStrategy =
      new FieldCopyStrategy(
          "id",
          "readableName",
          "questions",
          "question",
          "options",
          "option",
          "instruction",
          "text",
          "readMore",
          "comment",
          "description");
  /*
  merge collection by comparing id, using the method 'getId()' on each item inside a collection.
  used for merging translated coaches.
  */
  private static final MergeStrategy2 getterMergeStrategy = new GetterMergeStrategy("getId");
  private static final Questionnaire emptyCoach = new Questionnaire();

  /** Coach file instantiation, saves the file path of each coach for quick unmarshalling. */
  private static class Coach {
    private final Path path;
    private Questionnaire coach;

    public Coach(Path path) {
      this.path = path;
      this.coach = null;
    }
  }

  /**
   * Coach instantiation, saves the default coach and the corresponding libraries and translations.
   */
  private static class CoachCollection {
    private final String id;
    private String parent;
    private Coach defaultCoach;
    private final Mapper<Questionnaire> mapper;
    private final List<CoachLibrary> libraries;
    private final Map<String, Coach> translations;

    CoachCollection(String id) {
      this.id = id;
      this.parent = null;
      this.libraries = new ArrayList<>(1);
      this.translations = new HashMap<>();
      this.mapper = CacheFactory.createMapper(Questionnaire.class);
    }
  }

  static String getLanguage(Locale locale) {
    return locale != null ? locale.getLanguage().toLowerCase() : null;
  }

  static boolean isDefaultCoach(String language) {
    return language == null || language.equalsIgnoreCase("en");
  }

  private final ILibCal libCal;
  private final Map<String, CoachCollection> objectCache;

  CoachCache(Path dir, ILibCal libCal) throws Exception {
    super(dir);
    this.libCal = libCal;
    this.objectCache = new HashMap<>();

    // load from coach dir
    if (Files.exists(dir)) {
      if (!Files.isDirectory(dir)) {
        throw new CacheException(dir.toString() + " is not a directory.");
      }
      logger.info("Found existing coach directory " + dir.toString());
    } else {
      Files.createDirectory(dir);
      logger.info("Created new coach directory " + dir.toString());
    }
  }

  /**
   * Registers a new coach.
   *
   * @param coachId The coach identifier of the coach.
   * @param parentId The parent coach identifier of the coach.
   * @param language The language of the coach.
   * @param path The file path of the coach.
   */
  void addCoach(String coachId, String parentId, String language, Path path) {
    writeLock.lock();
    try {
      if (coachId == null || coachId.isEmpty()) {
        throw new IllegalArgumentException("Coach id must be not null or empty");
      }

      CoachCollection coachCollection = this.objectCache.get(coachId);
      if (coachCollection == null) {
        coachCollection = new CoachCollection(coachId);
        objectCache.put(coachId, coachCollection);
      }
      if (isDefaultCoach(language)) {
        coachCollection.parent = parentId;
        if (!path.endsWith(coachId + ".xml")) {
          logger.log(Level.WARNING, "Default coach file should be named with: " + coachId + ".xml");
        }
        logger.log(
            Level.INFO, "Registered default coach " + coachId + ", path: " + path.toString());
        coachCollection.defaultCoach = new Coach(path);
      } else {
        if (!path.endsWith(coachId + "_" + language + ".xml")) {
          logger.log(
              Level.WARNING,
              "Translated coach file should be named with: " + coachId + "_" + language + ".xml");
        }
        logger.log(
            Level.INFO,
            "Registered translated coach "
                + coachId
                + " ("
                + language
                + "), path: "
                + path.toString());
        coachCollection.translations.put(language, new Coach(path));
      }
    } finally {
      writeLock.unlock();
    }
  }

  //  /**
  //   * <p>Unregisters a registered coach.</p>
  //   *
  //   * @param coachId The coach identifier of the coach.
  //   * @param language The language of the coach.
  //   */
  //  void removeCoach(String coachId, String language) {
  //    writeLock.lock();
  //    try {
  //      if (isDefaultCoach(language)) {
  //        CoachCollection collection = objectCache.remove(coachId);
  //        if (collection != null) {
  //          logger.log(Level.INFO, "Removed default coach " + coachId + " and all translated
  // coaches [" +
  //              String.join(", ", collection.translations.keySet()) + "]");
  //        }
  //      } else {
  //        CoachCollection collection = objectCache.get(coachId);
  //        if (collection != null && collection.translations.remove(language) != null) {
  //          logger.log(Level.INFO, "Removed translated coach " + coachId + " (" + language +
  // ").");
  //        }
  //      }
  //    } finally {
  //      writeLock.unlock();
  //    }
  //  }

  /**
   * Unregisters a registered coach. First quick access is used, this means the coach id and
   * language are retrieved from the file name. If quick access does not find the coach, then the
   * coach will be searched inside the cache.
   *
   * @param path The coach identifier of the coach, which should be removed.
   */
  void removeCoach(Path path) {
    readLock.lock();
    try {
      // quick access using file name
      Tuple<String, String> coachIdLan = extractCoachIdLan(path);
      if (coachIdLan != null) {
        String coachId = coachIdLan.getFirst();
        CoachCollection collection = objectCache.get(coachId);
        if (collection != null) {
          String language = coachIdLan.getSecond();
          if (isDefaultCoach(language)) {
            // should be default
            if (collection.defaultCoach != null && collection.defaultCoach.path.equals(path)) {
              // test default coach path
              objectCache.remove(coachId);
              logger.log(
                  Level.INFO,
                  "Removed default coach "
                      + coachId
                      + " and all translated coaches ["
                      + String.join(", ", collection.translations.keySet())
                      + "], path: "
                      + path.toString());
              return;
            }
          } else {
            // should be translated
            Coach translation = collection.translations.get(language);
            if (translation != null && translation.path.equals(path)) {
              collection.translations.remove(language);
              logger.log(
                  Level.INFO,
                  "Removed translated coach "
                      + coachId
                      + " ("
                      + language
                      + "), path: "
                      + path.toString());
              return;
            }
          }
        }
      }
      logger.log(Level.FINE, "Quick access not available for path: " + path.toString());
      // search paths
      Iterator<Map.Entry<String, CoachCollection>> it = objectCache.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<String, CoachCollection> entry = it.next();
        CoachCollection collection = entry.getValue();
        // check default coach
        if (collection.defaultCoach != null && collection.defaultCoach.path.equals(path)) {
          it.remove();
          logger.log(
              Level.INFO,
              "Removed default coach "
                  + collection.id
                  + " and all translated coaches ["
                  + String.join(", ", collection.translations.keySet())
                  + "], path: "
                  + path.toString());
          break;
        }
        // check translated coach
        boolean removedTranslation = false;
        Iterator<Map.Entry<String, Coach>> itTranslated =
            collection.translations.entrySet().iterator();
        while (itTranslated.hasNext()) {
          Map.Entry<String, Coach> entryTranslated = itTranslated.next();
          if (entryTranslated.getValue().path.equals(path)) {
            itTranslated.remove();
            removedTranslation = true;
            logger.log(
                Level.INFO,
                "Removed translated coach "
                    + collection.id
                    + " ("
                    + entryTranslated.getKey()
                    + "), path: "
                    + path.toString());
            break;
          }
        }
        if (removedTranslation) {
          break;
        }
      }
    } finally {
      readLock.unlock();
    }
  }

  private static Tuple<String, String> extractCoachIdLan(Path path) {
    String name = path.getFileName().toString();
    if (!name.endsWith(".xml")) {
      return null;
    }
    String body = name.substring(0, name.length() - 4);
    int i = body.lastIndexOf('_');
    if (i < 0) {
      return new Tuple<>(body, null);
    } else {
      return new Tuple<>(body.substring(0, i), body.substring(i + 1));
    }
  }

  private CoachCollection getCollection(String coachId) throws CacheException {
    CoachCollection collection = this.objectCache.get(coachId);
    if (collection == null || collection.defaultCoach == null) {
      throw new CacheNotFoundException("Default coach " + coachId + " was not detected.");
    }
    return collection;
  }

  private Tuple<Questionnaire, List<CoachLibrary>> getDefault(String coachId) throws CacheException {
    CoachCollection collection = getCollection(coachId);
    Coach defaultCoach = collection.defaultCoach;
    // load default coach and libraries if not loaded yet
    synchronized (defaultCoach) {
      if (defaultCoach.coach != null && !collection.libraries.isEmpty()) {
        return new Tuple<>(defaultCoach.coach, collection.libraries);
      }
      // load default coach
      try {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (collection.parent != null) {
          // load parent coach libs
          Tuple<Questionnaire, List<CoachLibrary>> parent = getDefault(collection.parent);
          classLoader = parent.getSecond().get(0).getClass().getClassLoader();
        }

        logger.log(Level.INFO, "Loading default coach from file " + defaultCoach.path.toString());
        Questionnaire coach = collection.mapper.unmarshal(this.path.resolve(defaultCoach.path));
        List<CoachLibrary> libraries = new ArrayList<>(1);
        if (coach.getOrder() == null) {
          coach.setOrder(Integer.MAX_VALUE);
        }
        for (eu.smesec.bridge.generated.Library l : coach.getLibrary()) {
          // We encode base64binary (and return a string) because in most cases t
          // his is more useful to us.
          // In *this* instance however, we need the decoded value, so we do that here.
          logger.log(
              Level.INFO,
              "Loading library " + l.getId() + " inside coach " + defaultCoach.path.toString());
          CoachLibrary concreteLibrary = CacheFactory.loadLibrary(classLoader, l);
          // Run coach initialization routines
          concreteLibrary.init(l.getId(), coach, this.libCal, logger);
          libraries.add(concreteLibrary);
          logger.log(
              Level.INFO,
              "Loaded library "
                  + l.getId()
                  + " successfully inside coach "
                  + defaultCoach.path.toString());
        }
        // clear encoded libraries, since they are stored in a separate list
        coach.getLibrary().clear();
        defaultCoach.coach = coach;
        collection.libraries.addAll(libraries);
        logger.log(
            Level.INFO,
            "Loaded default coach "
                + coach.getId()
                + " successfully from file "
                + defaultCoach.path.toString());
        return new Tuple<>(coach, libraries);
      } catch (MapperException | LibraryException e) {
        throw new CacheException(e.getMessage());
      }
    }
  }

  private Questionnaire getTranslation(String coachId, String language) throws CacheException {
    CoachCollection collection = getCollection(coachId);
    Coach translation = collection.translations.get(language);
    if (translation != null) {
      synchronized (translation) {
        if (translation.coach != null) {
          return translation.coach;
        }
        // load translated coach
        try {
          logger.log(
              Level.INFO,
              "Loading translated coach "
                  + collection.id
                  + " ("
                  + language
                  + ") from file "
                  + translation.path.toString());
          Questionnaire coach = collection.mapper.unmarshal(this.path.resolve(translation.path));
          Questionnaire reduced = new Questionnaire();
          // copy only translated text text
          coach.copyTo(new DefaultRootObjectLocator(coach), reduced, fieldCopyStrategy);
          translation.coach = reduced;
          logger.log(
              Level.INFO,
              "Loaded translated coach " + collection.id + " (" + language + ") successfully ");
          return reduced;
        } catch (MapperException e) {
          logger.log(
              Level.WARNING,
              "Loading translated coach "
                  + collection.id
                  + " ("
                  + language
                  + ") failed: "
                  + e.getMessage()
                  + "\nUsing default coach "
                  + collection.id);
        }
      }
    }
    return emptyCoach;
  }

  private Tuple<Questionnaire, List<CoachLibrary>> getCoachLibs(String coachId, String language)
      throws CacheException {
    Tuple<Questionnaire, List<CoachLibrary>> defaultCoach = getDefault(coachId);
    Questionnaire translation = getTranslation(coachId, language);
    Questionnaire result = new Questionnaire();
    // merge translated texts into default copy
    result.mergeFrom(null, null, translation, defaultCoach.getFirst(), getterMergeStrategy);
    return new Tuple<>(result, new ArrayList<>(defaultCoach.getSecond()));
  }

  /**
   * Checks if a coach has been registered.
   *
   * @param coachId The coach identifier of the coach.
   * @param locale The language of the coach
   * @return <code>true</code> if the coach has been registered, or <code>false</code> otherwise.
   */
  public boolean existsCoach(String coachId, Locale locale) {
    readLock.lock();
    try {
      CoachCollection coachCollection = objectCache.get(coachId);
      if (coachCollection != null) {
        String language = getLanguage(locale);
        if (isDefaultCoach(language)) {
          return coachCollection.defaultCoach != null;
        } else {
          return coachCollection.translations.containsKey(language);
        }
      }
      return false;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Executes a read command on a coach.
   *
   * @param coachId relative coach directory.
   * @param locale preferred coach language.
   * @param command read command.
   * @param <R> command return type.
   * @return command result.
   * @throws CacheException if the coach could not be loaded.
   */
  public <R> R read(
      String coachId, Locale locale, ICommand<Tuple<Questionnaire, List<CoachLibrary>>, R> command)
      throws CacheException {
    readLock.lock();
    try {
      String language = getLanguage(locale);
      Tuple<Questionnaire, List<CoachLibrary>> coachLibs = getCoachLibs(coachId, language);
      return command.execute(coachLibs);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Executes a read command on all installed coaches.
   *
   * @param locale preferred coach language.
   * @param command read command.
   * @param <R> command return type.
   * @return command result.
   * @throws CacheException if a coach could not be loaded.
   */
  public <R> R readAll(
      Locale locale, ICommand<List<Tuple<Questionnaire, List<CoachLibrary>>>, R> command)
      throws CacheException {
    readLock.lock();
    try {
      String language = getLanguage(locale);
      List<Tuple<Questionnaire, List<CoachLibrary>>> coachLibList = new ArrayList<>();
      for (String coachId : objectCache.keySet()) {
        coachLibList.add(getCoachLibs(coachId, language));
      }
      return command.execute(coachLibList);
    } finally {
      readLock.unlock();
    }
  }
}
