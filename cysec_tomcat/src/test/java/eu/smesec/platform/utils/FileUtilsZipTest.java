package eu.smesec.platform.utils;

import eu.smesec.core.utils.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtilsZipTest {
  private Path source, temp;

  public FileUtilsZipTest() {
    this.source = Paths.get("src/test/resources/zip");
    this.temp = Paths.get("src/test/resources/temp_zip");
  }

  @Before
  public void setUp() {
    try {
      FileUtils.copyDir(source, temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @After
  public void tearDown() {
    try {
      FileUtils.deleteDir(temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }


  @Test
  public void testZip() {
    Path source = this.temp.resolve("archive");
    Path dest = this.temp.resolve("test.zip");
    Set<String> entries = new HashSet<>(Arrays.asList(
          "test_file.txt",
          "test_dir/test_file_1.txt",
          "test_dir/test_file_2.txt"
    ));
    try {
      FileUtils.zip(source, dest);

      Assert.assertTrue(Files.exists(dest));
      try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(dest))) {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
          String entryName = zipEntry.getName().replace("\\", "/");
          Assert.assertTrue(entries.remove(entryName));
          zipEntry = zis.getNextEntry();
        }
      }
      Assert.assertTrue(entries.isEmpty());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testZipExclusion() {
    Path source = this.temp.resolve("archive");
    Path dest = this.temp.resolve("test.zip");
    Set<String> entries = new HashSet<>(Arrays.asList(
          "test_file.txt",
          "test_dir/test_file_2.txt"
    ));
    try {
      FileUtils.zip(source, dest, "test_dir/test_file_1.txt");
      Assert.assertTrue(Files.exists(dest));
      try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(dest))) {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
          String entryName = zipEntry.getName().replace("\\", "/");
          Assert.assertTrue(entries.remove(entryName));
          zipEntry = zis.getNextEntry();
        }
      }
      Assert.assertTrue(entries.isEmpty());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testUnzip() {
    Path source = this.temp.resolve("archive.zip");
    Path dest = this.temp.resolve("test");
    Map<String, String> entries = new HashMap<>();
    entries.put("test_file.txt", "base content");
    entries.put("test_dir/test_file_1.txt", "content 1");
    entries.put("test_dir/test_file_2.txt", "content 2");
    try {
      Files.createDirectory(dest);
      FileUtils.unzip(source, dest);

      for(Map.Entry<String, String> entry : entries.entrySet()) {
        Path path = dest.resolve(entry.getKey());
        Assert.assertTrue(Files.exists(path));
        Assert.assertEquals(entry.getValue(), String.join("\n", Files.readAllLines(path)));
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }
}
