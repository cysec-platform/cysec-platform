package eu.smesec.core.cache;

import eu.smesec.bridge.execptions.CacheAlreadyExistsException;
import eu.smesec.bridge.execptions.CacheException;
import eu.smesec.bridge.execptions.CacheNotFoundException;
import eu.smesec.bridge.generated.Audits;
import eu.smesec.bridge.generated.Company;
import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.bridge.generated.User;
import eu.smesec.bridge.utils.TokenUtils;
import eu.smesec.core.threading.ThreadFactory;
import eu.smesec.core.threading.Timer;
import eu.smesec.core.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data cache: Handles root data directory. Thread safe read/write access.
 *
 * @author Claudio Seitz
 */
class DataCache extends Cache {

  private final Map<String, CompanyCache> companies;

  /**
   * Data cache constructor.
   *
   * @param path data root directory
   */
  DataCache(Path path) throws CacheException, IOException {
    super(path);
    this.companies = new HashMap<>();
    if (Files.exists(path)) {
      if (!Files.isDirectory(path)) {
        throw new CacheException(path + " is not a directory.");
      }
      logger.info(() -> "Loading existing cache directory " + path);
      List<Path> subDirs;
      // use try with resources to close stream automatically
      try (Stream<Path> stream = Files.list(path)) {
        subDirs = stream.filter(Files::isDirectory).collect(Collectors.toList());
      }
      // use for loop instead of lambda for better exception handling
      for (Path companyPath : subDirs) {
        String childId = FileUtils.getFileName(companyPath).toLowerCase();
        if (!companies.containsKey(childId)) {
          CompanyCache companyCache = CacheFactory.createCompanyCache(companyPath);
          companyCache.load();
          companies.put(childId, companyCache);
          logger.info(() -> "Added existing company " + childId);
        } else {
          logger.warning(() -> "Skipping already added company " + childId);
        }
      }
    } else {
      logger.info(() -> "Creating new cache directory " + path);
      Files.createDirectories(path);
      logger.info(() -> "Created new cache directory " + path);
    }

    Timer timer = ThreadFactory.createTimer("Autosave", 60000);
    timer.register(this::updateXml);
    timer.start();
  }

  /**
   * Executes a company command.
   *
   * @param companyId the id of the company
   * @param command the command object
   */
  <R> R executeOnCompany(String companyId, ICommand<CompanyCache, R> command)
      throws CacheException {
    readLock.lock();
    try {
      CompanyCache company = companies.get(companyId.toLowerCase());
      if (company != null) {
        return command.execute(company);
      } else {
        throw new CacheNotFoundException("Company " + companyId.toLowerCase() + " was not found");
      }
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Executes a company command on all companies.
   *
   * @param command the command object
   */
  <R> R executeOnAllCompanies(ICommand<Collection<Map.Entry<String, CompanyCache>>, R> command)
      throws CacheException {
    readLock.lock();
    try {
      return command.execute(companies.entrySet());
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Returns a set of all loaded company ids.
   *
   * @return Set of company ids
   */
  Collection<String> getCompanyIds() {
    readLock.lock();
    try {
      return new ArrayList<>(companies.keySet());
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Checks if a company exists.
   *
   * @param id the id of the company
   * @return <code>true</code> if the company id exists, or <code>false</code> otherwise
   */
  boolean existsCompany(String id) {
    readLock.lock();
    try {
      return companies.containsKey(id.toLowerCase());
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Adds a new company to the cache.
   *
   * @param id the id of the company
   * @param name the name of the company
   */
  void addCompany(String id, String name, User admin, Questionnaire companyCoach)
      throws CacheException {
    writeLock.lock();
    String cid = id.toLowerCase();
    try {
      if (!companies.containsKey(cid)) {
        Company company = new Company();
        company.setId(cid);
        company.setCompanyname(name);
        company.getUser().add(admin);
        CompanyCache companyCache = CacheFactory.createCompanyCache(path.resolve(cid));
        companyCache.install(company, new Audits(), TokenUtils.generateRandomAlphanumericToken(25));
        companyCache.load();
        companyCache.instantiateCoach(null, companyCoach, null);
        companies.put(cid, companyCache);
        logger.info(() -> "Added new company " + cid);
      } else {
        throw new CacheAlreadyExistsException("Company " + cid + " already exists");
      }
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Extracts the *.zip file and populates the company.
   *
   * @param zip The path of the zip file.
   * @throws IOException if an io error occurs.
   * @throws CacheAlreadyExistsException if the company already exists or an io error occurs during company loading.
   */
  void unzipCompany(Path zip) throws CacheException, IOException {
    writeLock.lock();
    String companyId = FileUtils.getNameExt(FileUtils.getFileName(zip))[0];
    Path companyPath = path.resolve(companyId);
    try {
      if (Files.exists(companyPath)) {
        Files.delete(zip);
        throw new CacheAlreadyExistsException("company " + companyId + " already exists");
      }
      Path tempPath = Files.createTempDirectory(path, companyId);
      FileUtils.unzip(zip, tempPath);
      Files.move(tempPath, companyPath, StandardCopyOption.ATOMIC_MOVE);
      Files.delete(zip);
      CompanyCache companyCache = CacheFactory.createCompanyCache(companyPath);
      companyCache.load();
      companies.put(companyId, companyCache);
    } finally {
      writeLock.unlock();
    }
  }

  //  /**
  //   * <p>Deletes a company from the cache.</p>
  //   *
  //   * @param id the id of the company
  //   */
  //  void deleteCompany(String id) throws CacheException {
  //    writeLock.lock();
  //    String cid = id.toLowerCase();
  //    try {
  //      CompanyCache company = companies.remove(cid);
  //      if (company != null) {
  //        company.destroy();
  //        logger.info("Removed company " + cid);
  //      } else {
  //        throw new CacheNotFoundException("Company " + cid + " was not found");
  //      }
  //    } finally {
  //      writeLock.unlock();
  //    }
  //  }
  //
  //  //todo: load/unload company

  /** Updates all xml files. */
  void updateXml() {
    readLock.lock();
    logger.log(Level.INFO, "auto saving companies");
    try {
      for (CompanyCache company : companies.values()) {
        company.updateXml();
      }
    } finally {
      readLock.unlock();
    }
  }
}
