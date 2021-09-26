package eu.smesec.cysec.platform.core.cache;

import eu.smesec.bridge.CoachLibrary;
import eu.smesec.cysec.platform.core.utils.FileResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;
import javax.inject.Singleton;

import org.glassfish.jersey.logging.LoggingFeature;

@Singleton
public class ResourceManager {
  private static final Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private final Path resourcePath;

  public ResourceManager(Path templatePath) {
    this.resourcePath = templatePath.resolve("res_coaches");
  }

  public Path getResourcePath() {
    return resourcePath;
  }

  /**
   * Registers the resources of a library.
   *
   * @param library library object
   */
  public void registerLibResources(CoachLibrary library) {
    Path dir = resourcePath.resolve(library.getQuestionnaire().getId() + "/" + library.getId());
    LibraryClassLoader classloader = (LibraryClassLoader) library.getClass().getClassLoader();
    classloader.forEachResource(
        (path, is) -> {
          // get all resources in assets and no dirs
          if (path.startsWith("assets") && !path.endsWith("/")) {
            try {
              // create static coach resource folder
              Files.createDirectories(dir);
              // create temp file
              Path tempFile = Files.createTempFile(dir, null, ".tmp");
              Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
              // move to concrete file
              Path file = dir.resolve(path);
              Files.createDirectories(file.getParent());
              Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING); // override if exists
              logger.info(
                  "Copied file "
                      + path
                      + " from library "
                      + library.getId()
                      + " to "
                      + file.toString());
            } catch (IOException ioe) {
              throw new RuntimeException(ioe);
            }
          }
        });
  }

  /**
   * Checks if a resource exists.
   *
   * @param coachId The id of the coach
   * @param libId The id of the library, normally the pakage name
   * @param path The path of the file, relative to the coach resource folder
   * @return <code>true</code> if the file exists, or <code>false otherwise</code>
   */
  public boolean hasResource(String coachId, String libId, String path) {
    return Files.exists(resourcePath.resolve(Paths.get(coachId, libId, path)));
  }

  /**
   * Reads the data of a file for file download.
   *
   * @param relative The path if the file: coachId/libraryId/relativeFilePath.
   * @return file response object, or <code>null</code> if the file does exists.
   * @throws IOException If another io error occurs.
   */
  public FileResponse getResource(Path relative) throws IOException {
    try {
      return new FileResponse(Files.readAllBytes(resourcePath.resolve(relative)));
    } catch (NoSuchFileException fne) {
      return null;
    }
  }
}
