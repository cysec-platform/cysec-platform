package eu.smesec.platform.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.ws.rs.core.StreamingOutput;
import org.glassfish.jersey.logging.LoggingFeature;

public final class FileUtils {
  protected static Logger logger = Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME);

  private FileUtils() {}

  /**
   * <p>Returns the filename or directory name of a path.</p>
   *
   * @param path source path
   * @return filename
   */
  public static String getFileName(Path path) {
    return path.getFileName().toString();
  }

  /**
   * <p>Returns the file extansion of the input path.</p>
   *
   * @param path file path
   * @return file extension or null if the path does not contain an extension
   */
  public static String getFileExt(Path path) {
    String str = path.toString();
    int i = str.lastIndexOf('.');
    return i > 0 ? str.substring(i + 1) : null;
  }

  /**
   * <p>Replaces the file extension as *.tmp</p>
   *
   * @param path The source path
   * @return tmp path
   */
  public static Path asTemp(Path path) {
    String name = getFileName(path);
    int i = name.lastIndexOf('.');
    return path.getParent().resolve(name.substring(0, i) + ".tmp");
  }

  /**
   * <p>Separates the the name of a file and its extension.</p>
   *
   * @param filename filename of the file
   * @return name and extension
   */
  public static String[] getNameExt(String filename) {
    int i = filename.lastIndexOf('.');
    if (i == -1) {
      throw new IllegalArgumentException("file name doesn't contain dot");
    }
    return new String[]{
          filename.substring(0, i),
          filename.substring(i + 1)
    };
  }

  /**
   * <p>Deletes a directory and all of its files and sub directories.</p>
   *
   * @param source The source directory.
   * @throws IOException If an IO error occurs.
   */
  public static void deleteDir(Path source) throws IOException {
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        Files.delete(file);
        logger.finest("deleted file " + file.toString());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException {
        Files.delete(dir);
        logger.finest("deleted directory " + dir.toString());
        return FileVisitResult.CONTINUE;
      }
    });
//    Files.walk(source)
//          .sorted(Comparator.reverseOrder())
//          .map(Path::toFile)
//          .forEach(File::delete);
  }

  /**
   * <p>Copies a directory from source to target.
   * Replaces existing files.</p>
   *
   * @param source The source directory.
   * @param target The target directory.
   * @throws IOException If an IO error occurs.
   */
  public static void copyDir(Path source, Path target) throws IOException {
    Objects.requireNonNull(source);
    Objects.requireNonNull(target);
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        Path newDir = target.resolve(source.relativize(dir));
        Files.createDirectories(newDir);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        Path targetFile = target.resolve(source.relativize(file));
        Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
        logger.finest("Copied file " + file.toString());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException {
        logger.finest("Copied directory " + dir.toString());
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * <p>Moves a directory from source to target.
   * Replaces existing files.</p>
   *
   * @param source The source directory.
   * @param target The target directory.
   * @throws IOException If an IO error occurs.
   */
  public static void moveDir(Path source, Path target) throws IOException {
    Objects.requireNonNull(source);
    Objects.requireNonNull(target);
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
            throws IOException {
        Path newDir = target.resolve(source.relativize(dir));
        Files.createDirectories(newDir);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException {
        Path targetFile = target.resolve(source.relativize(file));
        Files.move(file, targetFile, StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING);
        logger.finest("Moved file " + file.toString());
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc)
            throws IOException {
        Files.delete(dir);
        logger.finest("Moved directory " + dir.toString());
        return FileVisitResult.CONTINUE;
      }
    });
  }

  /**
   * <p>Zips a directory into an *.zip archive.</p>
   *
   * @param source the source directory.
   * @param dest the *.zip archive.
   * @throws IOException if an io error occurs.
   */
  public static void zip(Path source, Path dest, String... exclusions) throws IOException {
    if (source == null || dest == null) {
      throw new IllegalArgumentException("Invalid source directory or destination archive");
    }
    Set<Path> exclusionSet = Arrays.stream(exclusions).map(Paths::get).collect(Collectors.toSet());
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(dest,
          StandardOpenOption.WRITE,
          StandardOpenOption.CREATE))) {
      Files.walk(source)
            .filter(path1 -> !Files.isDirectory(path1))
            .forEach(path1 -> {
              Path rel = source.relativize(path1);
              if (!exclusionSet.contains(rel)) {
                ZipEntry zipEntry = new ZipEntry(rel.toString());
                try {
                  zos.putNextEntry(zipEntry);
                  Files.copy(path1, zos);
                  zos.closeEntry();
                } catch (IOException ze) {
                  throw new RuntimeException(ze);
                }
              }
            });
    }
  }

  /**
   * <p>Unzips an *.zip archive into a directory.</p>
   *
   * @param source the *.zip archive.
   * @param dest the destination directory.
   * @throws IOException if an io error occurs.
   */
  public static void unzip(Path source, Path dest) throws IOException {
    if (source == null || dest == null) {
      throw new IllegalArgumentException("Invalid source archive or destination directory");
    }
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(source,
          StandardOpenOption.READ))) {
      ZipEntry zipEntry = zis.getNextEntry();
      while (zipEntry != null) {
        String name = zipEntry.getName();
        Path path = dest.resolve(zipEntry.getName());
        if (!name.endsWith("/")) {
          Files.createDirectories(path.getParent());
          Files.copy(zis, path);
        }
        zipEntry = zis.getNextEntry();
      }
      zis.closeEntry();
    }
  }
}
