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

import java.util.Locale;

public class LocaleTest {
   @Test
   public void testIsLanguage() {
     try {
       Assert.assertTrue(LocaleUtils.isLanguage("EN"));
       Assert.assertTrue(LocaleUtils.isLanguage("En"));
       Assert.assertTrue(LocaleUtils.isLanguage("en"));

       Assert.assertTrue(LocaleUtils.isLanguage("FR"));
       Assert.assertTrue(LocaleUtils.isLanguage("Fr"));
       Assert.assertTrue(LocaleUtils.isLanguage("fr"));

       Assert.assertTrue(LocaleUtils.isLanguage("DE"));
       Assert.assertTrue(LocaleUtils.isLanguage("De"));
       Assert.assertTrue(LocaleUtils.isLanguage("de"));

       Assert.assertFalse(LocaleUtils.isLanguage(null));
       Assert.assertFalse(LocaleUtils.isLanguage(""));
       Assert.assertFalse(LocaleUtils.isLanguage("INV"));
     } catch (Exception e) {
       e.printStackTrace();
       Assert.fail();
     }
   }


  @Test
  public void testFromLanguageNull() {
    try {
      Assert.assertEquals(Locale.ENGLISH, LocaleUtils.fromString(null));
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testFromLanguageTag() {
    try {
      Assert.assertEquals("en", LocaleUtils.fromString("EN").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("En").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en-GB").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en_GB").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en-Gb").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en_Gb").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en-US").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en_US").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en-Us").getLanguage());
      Assert.assertEquals("en", LocaleUtils.fromString("en_Us").getLanguage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testFromLanguageTagInvalid() {
    try {
      Assert.assertEquals("en", LocaleUtils.fromString("INV").getLanguage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testFromLanguageTagFrench() {
    try {
      Assert.assertEquals("fr", LocaleUtils.fromString("fr").getLanguage());
      Assert.assertEquals("fr", LocaleUtils.fromString("fr-FR").getLanguage());
      Assert.assertEquals("fr", LocaleUtils.fromString("fr-Fr").getLanguage());
      Assert.assertEquals("fr", LocaleUtils.fromString("fr_FR").getLanguage());
      Assert.assertEquals("fr", LocaleUtils.fromString("fr_Fr").getLanguage());

      Assert.assertEquals("en", LocaleUtils.fromString("fr_INV").getLanguage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testFromLanguageTagGerman() {
    try {
      Assert.assertEquals("de", LocaleUtils.fromString("DE").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("De").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de_DE").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de_De").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de_de").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de-CH").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de-Ch").getLanguage());
      Assert.assertEquals("de", LocaleUtils.fromString("de-ch").getLanguage());

      Assert.assertEquals("en", LocaleUtils.fromString("de-INV").getLanguage());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

}
