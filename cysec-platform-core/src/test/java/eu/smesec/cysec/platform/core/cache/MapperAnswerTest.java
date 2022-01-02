/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

import eu.smesec.cysec.platform.bridge.generated.Answer;
import eu.smesec.cysec.platform.bridge.generated.Answers;
import eu.smesec.cysec.platform.bridge.generated.QuestionnaireReference;
import eu.smesec.cysec.platform.core.utils.FileUtils;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class MapperAnswerTest {
  private Path source;
  private Path temp;
  private Path file;

  @Rule
  public TestName name = new TestName();

  public MapperAnswerTest() {
    this.source = Paths.get("src/test/resources/data");
  }

  @Before
  public void setUp() {
    this.temp = Paths.get("src/test/resources/test-temp/AnswerCache/" + name.getMethodName());
    this.file = this.temp.resolve("xyz/lib-coach/default.xml");

    try {
      FileUtils.copyDir(source, temp);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testInit() {
    Mapper<Answers> mapper = new Mapper<>(Answers.class);
    Path path = this.temp.resolve("xyz/lib-coach/default-test.xml");
    String coachId = "lib-coach";
    String fileName = "/var/lib/cysec/coaches/lib-coach.xml";
    try {
      Assert.assertFalse(Files.exists(path));
      QuestionnaireReference reference = new QuestionnaireReference();
      reference.setQuestionnaireId(coachId);
      reference.setFilename(fileName);
      Answers source = new Answers();
      source.setQuestionnaireReference(reference);

      mapper.init(path, source);

      Assert.assertTrue(Files.exists(path));
      JAXBContext jc = JAXBContext.newInstance(Answers.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))) {
        Object object = unmarshaller.unmarshal(is);
        Assert.assertNotNull(object);
        Assert.assertTrue(object instanceof Answers);
        Answers answers = (Answers) object;
        Assert.assertEquals(coachId, answers.getQuestionnaireReference().getQuestionnaireId());
        Assert.assertEquals(fileName, answers.getQuestionnaireReference().getFilename());
      }
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testUnmarshal() {
    try {
      Assert.assertTrue(Files.exists(file));
      Mapper<Answers> mapper = new Mapper<>(Answers.class);

      Answers answers = mapper.unmarshal(file);

      Assert.assertNotNull(answers);
      Assert.assertEquals("lib-coach", answers.getQuestionnaireReference().getQuestionnaireId());
      Assert.assertEquals("/var/lib/cysec/coaches/lib-coach.xml",
            answers.getQuestionnaireReference().getFilename());
      Assert.assertEquals(2, answers.getAnswer().size());
      Assert.assertEquals("q1", answers.getAnswer().get(0).getQid());
      Assert.assertEquals("q2", answers.getAnswer().get(1).getQid());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testMarshal() {
    try {
      Answers source = new Answers();
      Answer answer = new Answer();
      answer.setQid("q3");
      answer.setText("example");
      source.getAnswer().add(answer);
      Mapper<Answers> mapper = new Mapper<>(Answers.class);

      mapper.marshal(file, source);

      JAXBContext jc = JAXBContext.newInstance(Answers.class);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file, StandardOpenOption.READ))) {
        Object object = unmarshaller.unmarshal(is);
        Assert.assertNotNull(object);
        Answers answers = (Answers) object;
        Assert.assertEquals(1, answers.getAnswer().size());
        Answer added = answers.getAnswer().get(0);
        Assert.assertEquals(answer.getQid(), added.getQid());
        Assert.assertEquals(answer.getText(), added.getText());
      }
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
