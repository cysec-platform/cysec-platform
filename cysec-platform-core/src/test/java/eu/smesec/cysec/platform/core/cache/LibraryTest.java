/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.CoachLibrary;
import eu.smesec.cysec.platform.bridge.generated.Library;
import eu.smesec.cysec.platform.core.utils.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LibraryTest {
  private Path source;
  private Path temp;

  public LibraryTest() {
    this.source = Paths.get("src/test/resources/coaches");
  }

  @Rule
  public TestName name = new TestName();

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/CoachCache/" + name.getMethodName());

    try {
      FileUtils.copyDir(source, temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  @Ignore("Package names in bridge changed and test resources need to be updated accordingly")
  public void testLoadLibrary() {
    Path file = this.temp.resolve("eu.smesec.library.company.CompanyLib.txt");
    try {
      Assert.assertTrue(Files.exists(file));
      String libId = FileUtils.getNameExt(file.getFileName().toString())[0];
      String libEnc = Files.readAllLines(file).get(0);

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      Library lib = new Library();
      lib.setId(libId);
      lib.setValue(libEnc);

      CoachLibrary concreteLibrary = CacheFactory.loadLibrary(classLoader, lib);

      Assert.assertNotNull(concreteLibrary);
      Assert.assertEquals(libId, concreteLibrary.getClass().getName());
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
}
