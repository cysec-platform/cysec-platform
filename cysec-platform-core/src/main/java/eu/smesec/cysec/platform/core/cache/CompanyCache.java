package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.execptions.CacheAlreadyExistsException;
import eu.smesec.cysec.platform.bridge.execptions.CacheException;
import eu.smesec.cysec.platform.bridge.execptions.CacheNotFoundException;
import eu.smesec.cysec.platform.bridge.execptions.CacheReadOnlyException;
import eu.smesec.cysec.platform.bridge.execptions.MapperException;
import eu.smesec.cysec.platform.bridge.generated.Answers;
import eu.smesec.cysec.platform.bridge.generated.Audits;
import eu.smesec.cysec.platform.bridge.generated.Company;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.core.utils.FileUtils;
import eu.smesec.cysec.platform.core.utils.FileResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jvnet.jaxb2_commons.lang.CopyTo2;

/**
 * Company cache: Handles the company directory. Thread safe read/write access.
 *
 * @author Claudio Seitz
 * @version 1.2
 */
class CompanyCache extends Cache {
  static final String USER_XML = "users.xml";
  static final String AUDITS_XML = "audits.xml";
  static final String DEFAULT_ANSWERS_XML = "default.xml";
  static final String REPLICA_TOKEN_FILE = "replica_token";
  static final String READ_ONLY_FILE = "readonly";

  private static class CachedObject<T> {
    private final Path path;
    private final Mapper<T> mapper;
    private T source;

    private CachedObject(Path path, Mapper<T> mapper) {
      this.path = path;
      this.mapper = mapper;
    }

    private void load() throws CacheException {
      try {
        this.source = mapper.unmarshal(path);
      } catch (MapperException me) {
        throw new CacheException(me.getMessage());
      }
    }

    private void save() throws CacheException {
      try {
        mapper.marshal(path, source);
      } catch (MapperException me) {
        throw new CacheException(me.getMessage());
      }
    }
  }

  private final Map<Path, CachedObject<?>> objectCache;
  private final AtomicLong userCount;
  private final String id;
  private String replicaToken;
  private volatile boolean readOnly;

  public CompanyCache(Path path) {
    super(path);
    this.userCount = new AtomicLong(1000);
    this.objectCache = new TreeMap<>();
    this.id = path.getFileName().toString();
    this.replicaToken = null;
    this.readOnly = false;
  }

  /**
   * Creates the company directory and all required files.
   *
   * @param company Company object containing admin user.
   * @param replicaToken Replica token value.
   * @throws CacheException If an error occurs during the company installation
   */
  void install(Company company, Audits audits, String replicaToken) throws CacheException {
    if (company == null || replicaToken == null || replicaToken.isEmpty()) {
      throw new IllegalArgumentException("company or replica token is invalid");
    }
    writeLock.lock();
    try {
      Files.createDirectories(this.path);
      CacheFactory.createMapper(Company.class).init(this.path.resolve(USER_XML), company);
      logger.log(
          Level.INFO,
          "Created user file: "
              + this.path.resolve(USER_XML).toString()
              + " for company "
              + company.getId());
      CacheFactory.createMapper(Audits.class).init(this.path.resolve(AUDITS_XML), audits);
      logger.log(
          Level.INFO,
          "Created audit file: "
              + this.path.resolve(AUDITS_XML).toString()
              + " for company "
              + company.getId());
      Files.write(
          this.path.resolve(REPLICA_TOKEN_FILE),
          replicaToken.getBytes(),
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE_NEW);
      logger.log(Level.INFO, "Created replika token file for company " + company.getId());
    } catch (IOException | MapperException e) {
      throw new CacheException(e.getMessage());
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Loads the replica token, the read only flag file and the users file.
   *
   * @throws CacheException If an io error occurs during the company loading.
   */
  void load() throws CacheException {
    writeLock.lock();
    try {
      readOnly = Files.exists(this.path.resolve(READ_ONLY_FILE));
      replicaToken = Files.readAllLines(this.path.resolve(REPLICA_TOKEN_FILE)).get(0);
      Company source = getSource(Paths.get(USER_XML), Company.class);
      if (source != null) {
        Optional<User> maximum =
            source.getUser().stream().max(Comparator.comparingLong(User::getId));
        this.userCount.set(maximum.map(User::getId).orElse(1000L));
      } else {
        this.userCount.set(1000L);
      }
    } catch (IOException ioe) {
      //        logger.log(Level.WARNING, ioe.getMessage(), ioe);
      logger.log(
          Level.WARNING, "No replica token defined in company " + path.getFileName().toString());
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Returns the next higher user id.
   *
   * @return the next higher user id
   */
  public long nextUserId() {
    return this.userCount.incrementAndGet();
  }

  /**
   * Return the replica token of this company.
   *
   * @return The replica token if the replica token file is present, or <code>null</code> otherwise.
   */
  public String getReplicaToken() {
    readLock.lock();
    try {
      return this.replicaToken;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Returns the readonly flag of this company.
   *
   * @return <code>true</code> if the company is marked as readonly, or <code>false</code>
   *     otherwise.
   */
  public boolean isReadOnly() {
    readLock.lock();
    try {
      return this.readOnly;
    } finally {
      readLock.unlock();
    }
  }

  /////////////////////
  //  read on caches //
  /////////////////////

  /**
   * Executes a read command on the users xml file.
   *
   * @param command The read command to execute.
   * @param <R> The return type of the read command.
   * @return The result of the read command.
   * @throws CacheException If an error occurs during the command execution.
   */
  <R> R readOnUsers(ICommand<Company, R> command) throws CacheException {
    return readOnCache(Paths.get(USER_XML), Company.class, command);
  }

  /**
   * Executes a read command on the audits xml file.
   *
   * @param command The read command to execute.
   * @param <R> The return type of the read command.
   * @return The result of the read command.
   * @throws CacheException If an error occurs during the command execution.
   */
  <R> R readOnAudits(ICommand<Audits, R> command) throws CacheException {
    return readOnCache(Paths.get(AUDITS_XML), Audits.class, command);
  }

  /**
   * Executes a read command on the answers xml file.
   *
   * @param relative The answer xml file path, relative to the coach directory.
   * @param command The read command to execute.
   * @param <R> The return type of the read command.
   * @return The result of the read command.
   * @throws CacheException If an error occurs during the command execution.
   */
  <R> R readOnAnswers(Path relative, ICommand<Answers, R> command) throws CacheException {
    return readOnCache(relative, Answers.class, command);
  }

  private <T extends CopyTo2, R> R readOnCache(Path path, Class<T> classOfT, ICommand<T, R> command)
      throws CacheException {
    if (path == null || classOfT == null || command == null) {
      throw new IllegalArgumentException("path, class or command is null");
    }
    readLock.lock();
    try {
      T source = getSource(path, classOfT);
      T copy = classOfT.cast(source.copyTo(source.createNewInstance()));
      return command.execute(copy);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Executes a read command on each answers xml file.
   *
   * @param command The read command to execute.
   * @param <R> The return type of the read command.
   * @return The result of the read command.
   * @throws CacheException If an error occurs during the command execution.
   */
  <R> R readOnAllAnswers(ICommand<Map<Path, Answers>, R> command) throws CacheException {
    readLock.lock();
    try {
      Map<Path, Answers> map = new TreeMap<>();
      /*
      use List<Path> for try-with-resource to close directory stream automatically
      and prevent DirectoryNotEmptyException
       */
      List<Path> subdirs;
      try (Stream<Path> stream = Files.list(path).filter(Files::isDirectory)) {
        subdirs = stream.collect(Collectors.toList());
      }
      // use foreach loop for proper cache exception handling
      for (Path subdir : subdirs) {
        visitCoachFiles(
            subdir,
            path1 -> {
              Path relative = path.relativize(path1);
              Answers source = getSource(relative, Answers.class);
              map.put(relative, (Answers) source.clone());
              return null;
            });
      }
      return command.execute(map);
    } catch (IOException ioe) {
      logger.log(Level.WARNING, ioe.getMessage(), ioe);
      throw new CacheException("Error during visiting instantiated coaches " + ioe.getMessage());
    } finally {
      readLock.unlock();
    }
  }

  private void visitCoachFiles(Path path, ICommand<Path, Void> command) throws CacheException {
    try {
      /*
      use List<Path> for try-with-resource to close directory stream automatically
      and prevent DirectoryNotEmptyException
       */
      List<Path> paths;
      try (Stream<Path> stream = Files.list(path)) {
        paths = stream.collect(Collectors.toList());
      }
      // use foreach loop for proper cache exception handling
      for (Path path1 : paths) {
        if (Files.isDirectory(path1)) {
          visitCoachFiles(path1, command);
        } else if (path1.getFileName().toString().endsWith(".xml")) {
          command.execute(path1);
        }
      }
    } catch (IOException ioe) {
      throw new CacheException("Error during visiting instantiated coaches: " + ioe.getMessage());
    }
  }

  //////////////////////
  //  write on caches //
  //////////////////////

  /**
   * Executes a write command on the users xml file.
   *
   * @param command The write command to execute.
   * @throws CacheException If an error occurs during the command execution.
   */
  void writeOnUsers(ICommand<Company, Void> command) throws CacheException {
    writeOnCache(Paths.get(USER_XML), Company.class, command);
  }

  /**
   * Executes a write command on the audit xml file.
   *
   * @param command The write command to execute.
   * @throws CacheException If an error occurs during the command execution.
   */
  void writeOnAudits(ICommand<Audits, Void> command) throws CacheException {
    writeOnCache(Paths.get(AUDITS_XML), Audits.class, command);
  }

  /**
   * Executes a write command on the answers xml file.
   *
   * @param relative The answer xml file path, relative to the coach directory.
   * @param command The write command to execute.
   * @throws CacheException If an error occurs during the command execution.
   */
  void writeOnAnswers(Path relative, ICommand<Answers, Void> command) throws CacheException {
    writeOnCache(relative, Answers.class, command);
  }

  private <T extends CopyTo2> void writeOnCache(
      Path path, Class<T> classOfT, ICommand<T, Void> command) throws CacheException {
    if (path == null || classOfT == null || command == null) {
      throw new IllegalArgumentException("path, class or command is null");
    }
    writeLock.lock();
    try {
      if (readOnly) {
        throw new CacheReadOnlyException("Company " + id + " is read only");
      }
      T source = getSource(path, classOfT);
      command.execute(source);
    } finally {
      writeLock.unlock();
    }
  }

  //////////////////////
  // internal methods //
  //////////////////////

  private <T> T getSource(Path path, Class<T> classOfT) throws CacheException {
    // synchronized because of multiple read operations
    synchronized (objectCache) {
      if (!objectCache.containsKey(path)) {
        Path path1 = this.path.resolve(path);
        CachedObject<T> cachedObject =
            new CachedObject<>(path1, CacheFactory.createMapper(classOfT));
        logger.log(Level.INFO, "Loading cache: " + cachedObject.path);
        cachedObject.load();
        objectCache.put(path, cachedObject);
        logger.log(Level.INFO, "Loaded  cache: " + cachedObject.path);
        return cachedObject.source;
      } else {
        return classOfT.cast(objectCache.get(path).source);
      }
    }
  }

  /**
   * Saves all cached source objects to their corresponding files and clears the cache
   *
   * <p>Ignores failed operations.
   */
  void saveCachedObjects() {
    writeLock.lock();
    try {
      // no synchronized necessary because of write lock
      for (CachedObject<?> cachedObject : objectCache.values()) {
        try {
          logger.log(Level.INFO, "Saving cache: " + cachedObject.path);
          cachedObject.save();
          logger.log(Level.INFO, "Saved  cache: " + cachedObject.path);
        } catch (CacheException e) {
          logger.log(Level.WARNING, e.getMessage(), e);
        }
      }
      objectCache.clear();
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Removes an source object from the cache and save to its corresponding file.
   *
   * @param path relative path to source object
   */
  void saveCachedObject(Path path) {
    if (path == null) {
      throw new IllegalArgumentException("path is null");
    }
    writeLock.lock();
    try {
      // no synchronized necessary because of write lock
      CachedObject<?> cachedObject = objectCache.remove(path);
      if (cachedObject != null) {
        logger.log(Level.INFO, "Saving cache: " + cachedObject.path);
        cachedObject.save();
        logger.log(Level.INFO, "Saved  cache: " + cachedObject.path);
      }
    } catch (CacheException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    } finally {
      writeLock.unlock();
    }
  }

  void updateXml() {
    writeLock.lock();
    try {
      logger.log(Level.FINE, "Saving company " + this.id);
      // no synchronized necessary because of write lock
      for (CachedObject<?> cachedObject : objectCache.values()) {
        try {
          logger.log(Level.FINE, "Saving cache: " + cachedObject.path);
          cachedObject.save();
          logger.log(Level.FINE, "Saved  cache: " + cachedObject.path);
        } catch (CacheException e) {
          logger.log(Level.WARNING, e.getMessage());
        }
      }
      logger.log(Level.FINE, "Saved  company " + this.id);
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Synchronizes a file in this directory.
   *
   * <p>If the file exists the file will be overwritten.
   *
   * <p>WARNING: The readonly flag will be ignored.
   *
   * @param relative The file path relative to the company directory.
   * @param inputStream The new file content.
   * @throws CacheException if the file already exists and can not be overwritten, or the
   *     synchronization fails.
   */
  void syncFile(Path relative, InputStream inputStream, boolean overwrite) throws CacheException {
    if (relative == null || inputStream == null) {
      throw new IllegalArgumentException("path or input is null");
    }
    Path file = path.resolve(relative);
    this.writeLock.lock();
    try {
      Path temp = Files.createTempFile(path, "upload", null);
      try {
        Files.copy(inputStream, temp, StandardCopyOption.REPLACE_EXISTING);
        if (overwrite) {
          Files.move(
              temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } else {
          Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
        }
      } catch (IOException ioe) {
        Files.delete(temp);
        throw ioe;
      }
    } catch (FileAlreadyExistsException aee) {
      throw new CacheException("Cannot overwrite file: " + relative.toString());
    } catch (Exception e) {
      throw new CacheException("Failed synchronizing file: " + relative.toString());
    } finally {
      this.writeLock.unlock();
    }
  }

  /**
   * Creates a response from a file.
   *
   * @param relative The file path relative to the company directory.
   * @return The file response object if the file was found, or <code>null</code> otherwise.
   * @throws CacheException If an error occurs during the response creation.
   */
  FileResponse createFileResponse(Path relative) throws CacheException {
    if (relative == null) {
      throw new IllegalArgumentException("path is null");
    }
    Path file = this.path.resolve(relative);
    this.readLock.lock();
    try {
      return new FileResponse(Files.readAllBytes(file));
    } catch (NoSuchFileException fne) {
      return null;
    } catch (Exception e) {
      throw new CacheException("Failed creating file response for: " + relative.toString());
    } finally {
      this.readLock.unlock();
    }
  }

  /**
   * Marks the company as readonly or readwrite. If the company is marked as readonly, then no write
   * commands can be executed anymore, but file synchronization still works.
   *
   * @param value <code>true</code> to mark the company as readonly, or <code>false</code> to mark
   *     the company as readwrite.
   */
  void setReadOnly(boolean value) {
    this.writeLock.lock();
    try {
      this.readOnly = value;
      if (value) {
        Files.createFile(this.path.resolve(READ_ONLY_FILE));
      } else {
        Files.delete(this.path.resolve(READ_ONLY_FILE));
      }
    } catch (IOException ioe) {
      logger.log(Level.WARNING, ioe.getMessage());
    } finally {
      this.writeLock.unlock();
    }
  }

  /**
   * Creates a *.zip file of this company. The token file and readonly file will be ignored.
   *
   * @param dest The path of the new zip file.
   * @throws CacheException If an error during the zipping occurs.
   */
  void zip(Path dest) throws CacheException {
    this.readLock.lock();
    try {
      FileUtils.zip(this.path, dest, REPLICA_TOKEN_FILE, READ_ONLY_FILE);
    } catch (Exception e) {
      throw new CacheException(
          "error during zipping company "
              + this.path.getFileName().toString()
              + ": "
              + e.getMessage());
    } finally {
      this.readLock.unlock();
    }
  }

  ////////////////////////
  // coach file methods //
  ////////////////////////

  /**
   * Instantiate a new answer.xml file from a given coach.
   *
   * @param parent relative parent coach directory.
   * @param coach coach object.
   * @param names file names, if no names are specified default.xml will be used.
   * @throws CacheNotFoundException if the parent coach was not found.
   * @throws CacheAlreadyExistsException if the coach directory already exists.
   * @throws CacheException If an io error or mapper error occurs during the instantiation.
   */
  void instantiateCoach(Path parent, Questionnaire coach, Set<String> names) throws CacheException {
    if (coach == null) {
      throw new IllegalArgumentException("path or coach is null");
    }
    Path parentDir = parent != null ? path.resolve(parent) : path;
    writeLock.lock();
    try {
      if (!Files.exists(parentDir)) {
        throw new CacheNotFoundException("Parent directory " + parentDir + " does not exists");
      }
      Path coachDir = parentDir.resolve(coach.getId());
      if (Files.exists(coachDir)) {
        throw new CacheAlreadyExistsException("Coach directory " + coachDir + " already exists");
      }
      Files.createDirectories(coachDir);
      Answers answers = CacheFactory.createAnswersFromCoach(coach);
      Mapper<Answers> mapper = CacheFactory.createMapper(Answers.class);
      if (names != null) {
        for (String name : names) {
          mapper.init(coachDir.resolve(name + ".xml"), answers);
        }
      } else {
        mapper.init(coachDir.resolve(DEFAULT_ANSWERS_XML), answers);
      }
    } catch (IOException ioe) {
      throw new CacheException("IO error during coach instantiation: " + ioe.getMessage());
    } catch (MapperException me) {
      throw new CacheException("Mapper error during coach instantiation: " + me.getMessage());
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Deletes an existing coach file.
   *
   * @param coach The coach path to delete, relative to the company directory.
   * @throws CacheException If the coach path is a directory or an io error occurs.
   */
  void deleteCoach(Path coach) throws CacheException {
    if (coach == null) {
      throw new IllegalArgumentException("Path is null");
    }
    Path coachDir = path.resolve(coach);
    writeLock.lock();
    try {
      if (!Files.isDirectory(coachDir)) {
        throw new CacheException("Path " + coachDir.toString() + " is not a directory");
      }
      objectCache.remove(coach);
      FileUtils.deleteDir(coachDir);
    } catch (IOException ioe) {
      throw new CacheException(
          "Error during deleting answer file " + coach.toString() + ": " + ioe.getMessage());
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Checks if an answer xml file is present.
   *
   * @param relative The path of the answer xml file, relative to the company directory.
   * @return <code>true</code> if the coach file is present, or <code>false</code> otherwise.
   */
  boolean isCoachInstantiated(Path relative) {
    readLock.lock();
    try {
      return Files.exists(this.path.resolve(relative));
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Lists all instantiated coach xml file paths.
   *
   * @return list of all answers xml file paths.
   * @throws CacheException If an io error occurs.
   */
  List<Path> listInstantiatedCoaches() throws CacheException {
    readLock.lock();
    try {
      List<Path> names = new ArrayList<>();
      /*
      use List<Path> for try-with-resource to close directory stream automatically
      and prevent DirectoryNotEmptyException
       */
      List<Path> subdirs;
      try (Stream<Path> stream = Files.list(path).filter(Files::isDirectory)) {
        subdirs = stream.collect(Collectors.toList());
      }
      // use foreach loop for proper cache exception handling
      for (Path subdir : subdirs) {
        visitCoachFiles(
            subdir,
            path1 -> {
              Path relative = path.relativize(path1);
              names.add(relative);
              return null;
            });
      }
      return names;
    } catch (IOException ioe) {
      logger.log(Level.WARNING, ioe.getMessage(), ioe);
      throw new CacheException("Error during visiting instantiated coaches " + ioe.getMessage());
    } finally {
      readLock.unlock();
    }
  }
}
