package eu.smesec.core.cache;

import eu.smesec.bridge.Library;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
//import javax.inject.Singleton;

//import org.glassfish.jersey.logging.LoggingFeature;

public class ResourceManager {
    private static final Logger logger = Logger.getLogger(ResourceManager.class.getName());

    private final Path resourcePath;

    private ResourceManager(Supplier<Path> templatePath) {
        this.resourcePath = templatePath.get().resolve("res_coaches");
    }

    public static synchronized void init(final Supplier<Path> templatePath) {
        class ResourceManagerFactory implements Supplier<ResourceManager> {
            private final ResourceManager resourceManagerInstance = new ResourceManager(templatePath);

            @Override
            public ResourceManager get() {
                return resourceManagerInstance;
            }
        }

        if (!(instance instanceof ResourceManagerFactory)) {
            instance = new ResourceManagerFactory();
        } else {
            throw new IllegalStateException("Already Initialized");
        }
    }

    private static Supplier<ResourceManager> instance = new ResourceManagerHolder();

    /**
     * Temp Placeholder supplier
     */
    private static final class ResourceManagerHolder implements Supplier<ResourceManager> {
        @Override
        public synchronized ResourceManager get() {
            if (instance instanceof ResourceManagerHolder)
                throw new IllegalStateException("Not Initialized");
            return instance.get();
        }
    }

    /**
     * Returns the instance
     *
     * @return ResourceManager instance
     */
    public static ResourceManager getInstance() {
        return instance.get();
    }


    public Path getResourcePath() {
        return resourcePath;
    }

    /**
     * Registers the resources of a library.
     *
     * @param library library object
     */
    public void registerLibResources(Library library) {
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
     * @param libId   The id of the library, normally the package name
     * @param path    The path of the file, relative to the coach resource folder
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
    public byte[] getResource(Path relative) throws IOException {
        try {
            return Files.readAllBytes(resourcePath.resolve(relative));
        } catch (NoSuchFileException fne) {
            return null;
        }
    }
}
