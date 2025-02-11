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
package eu.smesec.cysec.platform.core.utils;

import org.junit.Assert;
import org.junit.Test;

public class ValidatorTest {
  @Test
  public void testWord() {
    Assert.assertTrue(Validator.validateWord("Muster"));
    Assert.assertTrue(Validator.validateWord("Muster27"));
    Assert.assertTrue(Validator.validateWord("Muster_27"));
  }

  @Test
  public void testInvalidWord() {
    Assert.assertFalse(Validator.validateWord(null));
    Assert.assertFalse(Validator.validateWord("Hans Muster"));
    Assert.assertFalse(Validator.validateWord("Hans/Muster"));
    Assert.assertFalse(Validator.validateWord("HansMuster:pwd"));
  }

  @Test
  public void testWordSpace() {
    Assert.assertTrue(Validator.validateWordSpace("Muster"));
    Assert.assertTrue(Validator.validateWordSpace("Muster27"));
    Assert.assertTrue(Validator.validateWordSpace("Muster_27"));
    Assert.assertTrue(Validator.validateWordSpace("Muster 27"));
  }

  @Test
  public void testInvalidWordSpace() {
    Assert.assertFalse(Validator.validateWordSpace("Hans/Muster"));
    Assert.assertFalse(Validator.validateWordSpace("Hans / Muster"));
    Assert.assertFalse(Validator.validateWordSpace("Hans Muster:pwd"));
  }

  @Test
  public void testEmail() {
    Assert.assertTrue(Validator.validateEmail("hans.muster@example.com"));
    Assert.assertTrue(Validator.validateEmail("muster@example.com"));
    Assert.assertTrue(Validator.validateEmail("hans.muster.lol@example.com"));
    Assert.assertTrue(Validator.validateEmail("hans.muster@students.example.com"));
  }

  @Test
  public void testInvalidEmail() {
    Assert.assertFalse(Validator.validateEmail("hans.muster.example.com"));
    Assert.assertFalse(Validator.validateEmail("hans.muster@com"));
    Assert.assertFalse(Validator.validateEmail("hans.muster@.com"));
    Assert.assertFalse(Validator.validateEmail("@example.com"));
    Assert.assertFalse(Validator.validateEmail("test.@example.com"));
  }

  @Test
  public void testAnswer() {
    Assert.assertTrue(Validator.validateAnswer("Hello World"));
    Assert.assertTrue(Validator.validateAnswer("Answer: \"My answer. \""));
    Assert.assertTrue(Validator.validateAnswer("answer 1 (Detailed explanation)"));
  }

  @Test
  public void testInvalidAnswer() {
    Assert.assertFalse(Validator.validateAnswer("Hello World!"));
    Assert.assertFalse(Validator.validateAnswer("a&b"));
    Assert.assertFalse(Validator.validateAnswer("<"));
    Assert.assertFalse(Validator.validateAnswer(">"));
    Assert.assertFalse(Validator.validateAnswer("hans/muster"));
    Assert.assertFalse(Validator.validateAnswer(";hansmuster"));
    Assert.assertFalse(Validator.validateAnswer("hansmuster?"));
    Assert.assertFalse(Validator.validateAnswer("hansmuster*"));
    Assert.assertFalse(Validator.validateAnswer("\"><script> </script><user name=\""));
  }
}
