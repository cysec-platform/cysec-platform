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
package eu.smesec.cysec.platform.core.config;

import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * booleanConfigHandlings for CySeC.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
public class ConfigTest {
  private static Config config;

  @BeforeClass
  public static void setUp() {
    try {
      config = new Config(CysecConfig.RESOURCE_FOLDER + "/" + CysecConfig.RESOURCE_FILE);
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void basicConfigHandling() {
    try {
      Config c2 = config.copy();
      // FIX this test. Currently there is no equals implementation
      // assertTrue("error comparing hashmaps", config.equals(c2));
    } catch (Exception e) {
      fail("should not raise an exception (" + e + ")");
    }
    
  }
  
  @Test
  public void stringConfigHandling() {
    try {
      config.getStringValue(null,"stringConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      config.setStringValue(null, "StringConfigHandling", "test", -1);
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    try {
      // String
      assertTrue("Should return true on first creation", config.createStringConfigValue("stringConfigHandling", "def", "def"));
      assertFalse("Should return false on recreation", config.createStringConfigValue("stringConfigHandling", "otherdef", "otherdef"));
      assertTrue("Should return true as default value", "def".equals(config.getStringValue(null,"stringConfigHandling")));
      assertTrue("Should return true as last value", "def".equals(config.setStringValue(null, "stringConfigHandling", "otherval", -1)));
      assertTrue("Should return false as last value", "otherval".equals(config.setStringValue(null, "stringConfigHandling", "thirdval", -1)));
      assertTrue("Should return false as last value", "thirdval".equals(config.getStringValue(null, "stringConfigHandling")));
      assertTrue("Should return false as last value", "thirdval".equals(config.setStringValue(null, "stringConfigHandling", "fourthval", -1)));
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }
    
    try {
      config.setBooleanValue(null, "stringConfigHandling", true, -1);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
    try {
      config.getBooleanValue(null, "stringConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
  }
  
  
  @Test
  public void numericConfigHandling() {
    try {
      config.getNumericValue(null, "numericConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      config.setNumericValue(null, "numericConfigHandling", 5, -1);
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    try {
      // numeric
      try {
        config.createNumericConfigValue("numericConfigHandling", "def", 5);
      } catch (Exception e) {
        e.printStackTrace();
        fail("got unexpected exception while creating numeric config");
      }
      try {
        config.createNumericConfigValue("numericConfigHandling", "def", 5);
        fail("command did unexpectedly succeed");
      } catch (IllegalArgumentException e) {
        // this is expected
      } catch (Exception e) {
        fail("got unexpected exception while creating numeric config");
      }
      
      assertTrue("Should return true as default value", config.getNumericValue(null,"numericConfigHandling") == 5);
      assertTrue("Should return true as last value", config.setNumericValue(null,"numericConfigHandling", 10, -1) == 5);
      assertTrue("Should return false as last value", config.setNumericValue(null,"numericConfigHandling", 15, -1) == 10);
      assertTrue("Should return false as last value", config.getNumericValue(null,"numericConfigHandling") == 15);
      assertTrue("Should return false as last value", config.setNumericValue(null,"numericConfigHandling", 20, -1) == 15);
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }
    
    try {
      config.setBooleanValue(null,"numericConfigHandling", true, -1);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
    try {
      config.getBooleanValue(null,"numericConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
  }
  
  @Test
  public void booleanConfigHandling() {
    try {
      config.getBooleanValue(null,"booleanConfigHandling");
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      e.printStackTrace();
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      config.setBooleanValue(null,"booleanConfigHandling", true, -1);
      fail("should raise NPE but nothing happened");
    } catch (NullPointerException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise NPE but a different exception is raised (" + e + ")");
    }
    
    try {
      //Boolean
      try {
        config.createBooleanConfigValue("booleanConfigHandling", "", true);
      } catch (Exception e) {
        fail("Should return true on first creation");
      }
      try {
        config.createBooleanConfigValue("booleanConfigHandling", "", false);
        fail("Should not  be successful on recreation");
      } catch (Exception e) {
        // this exception is intended
      }
      assertTrue("Should return true as default value", config.getBooleanValue(null,"booleanConfigHandling"));
      assertTrue("Should return true as last value", config.setBooleanValue(null,"booleanConfigHandling", false, -1));
      assertFalse("Should return false as last value", config.setBooleanValue(null,"booleanConfigHandling", false, -1));
      assertFalse("Should return false as last value", config.getBooleanValue(null,"booleanConfigHandling"));
      assertFalse("Should return false as last value", config.setBooleanValue(null,"booleanConfigHandling", true, -1));
      assertTrue("Should return true as last value", config.setBooleanValue(null,"booleanConfigHandling", true, -1));
      assertTrue("Should return true as last value", config.getBooleanValue(null,"booleanConfigHandling"));
      
    } catch (Exception e) {
      e.printStackTrace();
      fail("should not raise an exception but did (" + e + ")");
    }
    
    try {
      config.setStringValue(null,"booleanConfigHandling", "test", -1);
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
    try {
      config.getStringValue(null,"booleanConfigHandling");
      fail("should raise CCE but nothing happened");
    } catch (ClassCastException npe) {
      // all OK this is expected
    } catch (Exception e) {
      fail("should raise CCE but a different exception is raised (" + e + ")");
    }
    
  }
  
}
