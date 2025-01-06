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
package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.execptions.CacheNotFoundException;
import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.bridge.generated.User;
import eu.smesec.cysec.platform.core.threading.ThreadFactory;
import eu.smesec.cysec.platform.core.threading.Timer;
import eu.smesec.cysec.platform.core.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CacheFactory.class, ThreadFactory.class})
@PowerMockIgnore({"javax.xml.*"})
public class DataCacheTest {
  private Path source;
  private Path temp;
  private CompanyCache xyz;
  private Timer timer;

  @Rule
  public TestName name = new TestName();

  public DataCacheTest() {
    this.source = Paths.get("src/test/resources/data");

    try {
      xyz = Mockito.mock(CompanyCache.class);
      timer = Mockito.mock(Timer.class);

      PowerMockito.mockStatic(CacheFactory.class);
      Mockito.when(CacheFactory.createCompanyCache(Mockito.any())).thenReturn(xyz);

      PowerMockito.mockStatic(ThreadFactory.class);
      Mockito.when(ThreadFactory.createTimer(Mockito.anyString(), Mockito.anyLong())).thenReturn(timer);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/DataCache/" + name.getMethodName());

    try {
      FileUtils.copyDir(source, temp);
      Mockito.reset(xyz, timer);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInstall() {
    Path path = temp.resolve("data-test");
    try {
      Assert.assertFalse(Files.exists(path));
      new DataCache(path);
      Assert.assertTrue(Files.exists(path));
      Assert.assertTrue(Files.isDirectory(path));
      PowerMockito.verifyStatic(CacheFactory.class, Mockito.times(0));
      CacheFactory.createCompanyCache(Mockito.any());
      PowerMockito.verifyStatic(ThreadFactory.class, Mockito.times(1));
      ThreadFactory.createTimer(Mockito.anyString(), Mockito.anyLong());
      Mockito.verify(timer, Mockito.times(1)).start();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testParse() {
    try {
      Assert.assertTrue(Files.exists(temp));
      Assert.assertTrue(Files.isDirectory(temp));
      new DataCache(temp);
      PowerMockito.verifyStatic(CacheFactory.class, Mockito.times(2));
      CacheFactory.createCompanyCache(Mockito.any());
      PowerMockito.verifyStatic(ThreadFactory.class, Mockito.times(1));
      ThreadFactory.createTimer(Mockito.anyString(), Mockito.anyLong());
      Mockito.verify(timer, Mockito.times(1)).start();
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testGetCompanies() {
    String companyId1 = "xyz";
    String companyId2 = "ro";
    try {
      DataCache cache = new DataCache(temp);
      Collection<String> companyIds = cache.getCompanyIds();
      Assert.assertArrayEquals(new String[]{
            companyId1,
            companyId2,
      }, companyIds.toArray());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testExistsCompany() {
    String companyId = "xyz";
    try {
      DataCache cache = new DataCache(temp);
      Assert.assertTrue(cache.existsCompany(companyId));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testNotExistsCompany() {
    String companyId = "invalid";
    try {
      DataCache cache = new DataCache(temp);
      Assert.assertFalse(cache.existsCompany(companyId));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testExecuteOnCompany() {
    String companyId = "xyz";
    try {
      DataCache cache = new DataCache(temp);
      cache.executeOnCompany(companyId, companyCache -> {
        Assert.assertSame(xyz, companyCache);
        return null;
      });
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testExecuteOnInvalidCompany() {
    String companyId = "invalid";
    try {
      DataCache cache = new DataCache(temp);
      cache.executeOnCompany(companyId, companyCache -> {
        Assert.fail();
        return null;
      });
    } catch (Exception e) {
      Assert.assertEquals(CacheNotFoundException.class, e.getClass());
      Assert.assertEquals("Company " + companyId + " was not found", e.getMessage());
    }
  }

  @Test
  public void testAddCompany() {
    String companyId = "test";
    String companyName = "TEST";
    try {
      User admin = Mockito.mock(User.class);
      Questionnaire companyCoach = Mockito.mock(Questionnaire.class);
      PowerMockito.when(companyCoach.getId()).thenReturn("lib-company");

      DataCache cache = new DataCache(temp);
      cache.addCompany(companyId, companyName, admin, companyCoach);

      Assert.assertTrue(cache.existsCompany(companyId));
      PowerMockito.verifyStatic(CacheFactory.class, Mockito.times(3));
      CacheFactory.createCompanyCache(Mockito.any());
      Mockito.verify(xyz, Mockito.times(1))
            .instantiateCoach(null, companyCoach, null);
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
