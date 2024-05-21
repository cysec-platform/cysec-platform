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
package eu.smesec.cysec.platform.core.cache;

import eu.smesec.cysec.platform.bridge.generated.Questionnaire;
import eu.smesec.cysec.platform.core.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MapperCoachTest {
  private Path source;
  private Path temp;

  @Rule
  public TestName name = new TestName();

  public MapperCoachTest() {
    this.source = Paths.get("src/test/resources/coaches");
  }

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
  public void testUnmarshal() {
    try {
      Path file = this.temp.resolve("lib-company/lib-company.xml");
      Assert.assertTrue(Files.exists(file));
      Mapper<Questionnaire> mapper = new Mapper<>(Questionnaire.class);

      Questionnaire coach = mapper.unmarshal(file);

      Assert.assertNotNull(coach);
      Assert.assertEquals("lib-company", coach.getId());
      Assert.assertEquals("Company", coach.getReadableName());
      Assert.assertEquals(2, coach.getVersion());
      Assert.assertEquals(26, coach.getQuestions().getQuestion().size());
      Assert.assertEquals(1, coach.getLibrary().size());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testUnmarshal2() {
    Path file = this.temp.resolve("lib-company/lib-company_fr.xml");
    try {
      Assert.assertTrue(Files.exists(file));
      Mapper<Questionnaire> mapper = new Mapper<>(Questionnaire.class);

      Questionnaire coach = mapper.unmarshal(file);

      Assert.assertNotNull(coach);
      Assert.assertEquals("lib-company", coach.getId());
      Assert.assertEquals("Company", coach.getReadableName());
//      Assert.assertEquals(2, coach.getVersion());
//      Assert.assertEquals(26, coach.getQuestions().getQuestion().size());
//      Assert.assertEquals(1, coach.getLibrary().size());
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
