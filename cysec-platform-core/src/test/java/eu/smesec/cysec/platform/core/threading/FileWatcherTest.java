/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.threading;

import eu.smesec.cysec.platform.core.utils.FileUtils;
import org.junit.*;
import org.junit.rules.TestName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class FileWatcherTest {
  private FileWatcher watcher;
  private Path temp;

  @Rule
  public TestName name = new TestName();

  @Before
  public void setup() {
    this.watcher = ThreadFactory.createFileWatcher();
    this.temp = Paths.get("src/test/resources/test-temp/FileWatcher/" + name.getMethodName());

    try {
      Files.createDirectories(temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testCreateFile() {
    ignoreTestOnMac();
    BlockingDeque<Path> queue = new LinkedBlockingDeque<>(1);
    Path path = Paths.get("test.text");
    try {
      watcher.register(temp);
      watcher.start();
      watcher.registerOnCreate(queue::offer);
      Files.createFile(temp.resolve(path));
      Path path1 = queue.poll(200, TimeUnit.MILLISECONDS);
      Assert.assertEquals(temp.resolve(path), path1);
      watcher.unregister(temp);
      watcher.stop();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testModifyFile() {
    ignoreTestOnMac();
    BlockingDeque<Path> queue = new LinkedBlockingDeque<>(1);
    Path path = Paths.get("test.text");
    try {
      watcher.register(temp);
      watcher.start();
      watcher.registerOnModify(queue::offer);
      Files.createFile(temp.resolve(path));
      Files.write(temp.resolve(path), "Content".getBytes());
      Path path1 = queue.poll(200, TimeUnit.MILLISECONDS);
      Assert.assertEquals(temp.resolve(path), path1);
      watcher.unregister(temp);
      watcher.stop();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testDeleteFile() {
    ignoreTestOnMac();
    BlockingDeque<Path> queue = new LinkedBlockingDeque<>(1);
    Path path = Paths.get("test.text");
    try {
      watcher.register(temp);
      watcher.start();
      watcher.registerOnDelete(queue::offer);
      Files.createFile(temp.resolve(path));
      Files.delete(temp.resolve(path));
      Path path1 = queue.poll(200, TimeUnit.MILLISECONDS);
      Assert.assertEquals(temp.resolve(path), path1);
      watcher.unregister(temp);
      watcher.stop();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testDir() {
    ignoreTestOnMac();
    BlockingDeque<Path> queue = new LinkedBlockingDeque<>(2);
    Path dir = Paths.get("testdir");
    Path file = Paths.get("testdir/test.txt");
    try {
      watcher.start();
      watcher.register(temp);
      watcher.registerOnCreate(path -> {
        if (Files.isDirectory(path)) {
          watcher.register(path);
        }
        queue.offer(path);
      });
      Files.createDirectories(temp.resolve(dir));
      // Important: directory must be registered first
      // Or implement a wrapper and use condition variables
      Thread.sleep(200);
      Files.createFile(temp.resolve(file));
      Files.delete(temp.resolve(file));
      Files.delete(temp.resolve(dir));
      Path path1 = queue.poll(200, TimeUnit.MILLISECONDS);
      Path path2 = queue.poll(200, TimeUnit.MILLISECONDS);
      Assert.assertEquals(temp.resolve(dir), path1);
      Assert.assertEquals(temp.resolve(file), path2);
      watcher.unregister(temp);
      watcher.stop();
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

  /**
   * Since there is no native implementation of a WatchService for Mac OS, a polling mechanism is used internally.
   * As a result, the detection does not work near real-time as on Windows or Linus. There would be an option to
   * register with SensitivityWatchEventModifier.HIGH but this enumeration is only available in the
   * com.sun.nio.file package. Adding this to productive code which is not intended to run directly on Mac OS
   * does not add any benefit. Therefore, we ignore these tests when run on Mac OS.
   */
  private void ignoreTestOnMac() {
    final String osName = System.getProperty("os.name");
    Assume.assumeFalse(osName.toLowerCase().contains("mac"));
    Assume.assumeFalse(osName.toLowerCase().contains("darwin"));
  }

}
