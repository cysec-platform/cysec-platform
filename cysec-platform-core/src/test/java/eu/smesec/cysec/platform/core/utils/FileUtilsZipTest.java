/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.smesec.cysec.platform.core.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
