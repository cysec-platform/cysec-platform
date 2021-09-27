/*-
 * #%L
 * CYSEC Platform Bridge
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
package eu.smesec.cysec.platform.bridge;

import eu.smesec.cysec.platform.bridge.generated.Metadata;
import eu.smesec.cysec.platform.bridge.generated.Mvalue;
import eu.smesec.cysec.platform.bridge.generated.ObjectFactory;
import eu.smesec.cysec.platform.bridge.md.MetadataUtils;

import eu.smesec.cysec.platform.bridge.md.annotations.MdId;
import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class MetadataUtilsTest {
  private String stringType;
  private String binType;

  public MetadataUtilsTest() {
    this.stringType = "stringValue";
    this.binType = "binaryValue";
  }

  /* FIXME : Why test depreciated stuff????
  @Test
  public void testCreateMvalueStr() {
    String key = "mv-key";
    String value = "value";
    Mvalue mv = MetadataUtils.createMvalueStr(key, value);
    Assert.assertEquals(key, mv.getKey());
    Assert.assertEquals(stringType, mv.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals(value, mv.getStringValueOrBinaryValue().getValue());
  }

  @Test
  public void testCreateMvalueStrNull() {
    String key = "mv-key";
    Mvalue mv = MetadataUtils.createMvalueStr(key, null);
    Assert.assertEquals(key, mv.getKey());
    Assert.assertEquals(stringType, mv.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertNull(mv.getStringValueOrBinaryValue().getValue());
  }

  @Test
  public void testCreateMvalueStrKeyNull() {
    String value = "value";
    try {
      MetadataUtils.createMvalueStr(null, value);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testCreateMvalueStrKeyEmpty() {
    String value = "value";
    try {
      MetadataUtils.createMvalueStr("", value);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testCreateMvalueBin() {
    String key = "mv-key";
    String value = "JUZGFGB==";
    Mvalue mv = MetadataUtils.createMvalueBin(key, value);
    Assert.assertEquals(key, mv.getKey());
    Assert.assertEquals(binType, mv.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals(value, mv.getStringValueOrBinaryValue().getValue());
  }

  @Test
  public void testCreateMvalueBinNull() {
    String key = "mv-key";
    Mvalue mv = MetadataUtils.createMvalueBin(key, null);
    Assert.assertEquals(key, mv.getKey());
    Assert.assertEquals(binType, mv.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertNull(mv.getStringValueOrBinaryValue().getValue());
  }

  @Test
  public void testCreateMvalueBinKeyNull() {
    String value = "JUZGFGB==";
    try {
      MetadataUtils.createMvalueBin(null, value);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testCreateMvalueBinKeyEmpty() {
    String value = "JUZGFGB==";
    try {
      MetadataUtils.createMvalueBin("", value);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertNotNull(e);
    }
  }

  @Test
  public void testCreateMetadata() {
    Mvalue mv1 = Mockito.mock(Mvalue.class);
    Mvalue mv2 = Mockito.mock(Mvalue.class);
    Mockito.when(mv1.getKey()).thenReturn("mv-key1");
    Mockito.when(mv2.getKey()).thenReturn("mv-key2");

    String key = "md-key";
    List<Mvalue> values = Arrays.asList(mv1, mv2);
    Metadata md = MetadataUtils.createMetadata(key, values);

    Assert.assertEquals(key, md.getKey());
    Assert.assertTrue(md.getMvalue().contains(mv1));
    Assert.assertTrue(md.getMvalue().contains(mv2));
  }

  @Test
  public void testCreateMetadataMultipleMvalueKey() {
    Mvalue mv1 = Mockito.mock(Mvalue.class);
    Mvalue mv2 = Mockito.mock(Mvalue.class);
    Mvalue mv3 = Mockito.mock(Mvalue.class);
    Mockito.when(mv1.getKey()).thenReturn("mv-key1");
    Mockito.when(mv2.getKey()).thenReturn("mv-key2");
    Mockito.when(mv3.getKey()).thenReturn("mv-key2");

    String key = "md-key";
    List<Mvalue> values = Arrays.asList(mv1, mv2, mv3);
    Metadata md = MetadataUtils.createMetadata(key, values);

    Assert.assertEquals(key, md.getKey());
    Assert.assertTrue(md.getMvalue().contains(mv1));
    Assert.assertFalse(md.getMvalue().contains(mv2));
    Assert.assertTrue(md.getMvalue().contains(mv3));
  }
  */
  private static ObjectFactory factory = new ObjectFactory();

  @MdNamespace("testnamespace")
  public static class TestClass {
    @MdId public String id;
    @MvKey("name")
    public String name;
    @MvKey("number")
    public int num;
    @MvKey("flag")
    public boolean f;
  }

  @Test
  public void testToMd() {
    TestClass testClass = new TestClass();
    testClass.id = "testId";
    testClass.name = "testname";
    testClass.num = 876;
    testClass.f = true;


    Metadata md = MetadataUtils.toMd(testClass);
    Assert.assertEquals("testnamespace.testId", md.getKey());
    Assert.assertEquals(3, md.getMvalue().size());
    Mvalue mv0 = md.getMvalue().get(0);
    Assert.assertEquals("name", mv0.getKey());
    Assert.assertEquals("stringValue", mv0.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals("testname", mv0.getStringValueOrBinaryValue().getValue());
    Mvalue mv1 = md.getMvalue().get(1);
    Assert.assertEquals("number", mv1.getKey());
    Assert.assertEquals("stringValue", mv1.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals("876", mv1.getStringValueOrBinaryValue().getValue());
    Mvalue mv2 = md.getMvalue().get(2);
    Assert.assertEquals("flag", mv2.getKey());
    Assert.assertEquals("stringValue", mv2.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals("true", mv2.getStringValueOrBinaryValue().getValue());
  }

  @Test
  public void testToMd2() {
    TestClass testClass = new TestClass();
    testClass.id = "testId";
    testClass.num = 876;

    Metadata md = MetadataUtils.toMd(testClass);
    Assert.assertEquals("testnamespace.testId", md.getKey());
    Assert.assertEquals(2, md.getMvalue().size());
    Mvalue mv0 = md.getMvalue().get(0);
    Assert.assertEquals("number", mv0.getKey());
    Assert.assertEquals("stringValue", mv0.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals("876", mv0.getStringValueOrBinaryValue().getValue());
    Mvalue mv1 = md.getMvalue().get(1);
    Assert.assertEquals("flag", mv1.getKey());
    Assert.assertEquals("stringValue", mv1.getStringValueOrBinaryValue().getName().getLocalPart());
    Assert.assertEquals("false", mv1.getStringValueOrBinaryValue().getValue());
  }

  @Test
  public void testFromMd() {
    Metadata md = factory.createMetadata();
    md.setKey("testnamespace.testId");
    Mvalue mv0 = factory.createMvalue();
    mv0.setKey("name");
    mv0.setStringValueOrBinaryValue(factory.createMvalueStringValue("testname"));
    Mvalue mv1 = factory.createMvalue();
    mv1.setKey("number");
    mv1.setStringValueOrBinaryValue(factory.createMvalueStringValue("876"));
    Mvalue mv2 = factory.createMvalue();
    mv2.setKey("flag");
    mv2.setStringValueOrBinaryValue(factory.createMvalueStringValue("true"));
    md.getMvalue().addAll(Arrays.asList(mv0, mv1, mv2));

    TestClass testClass = MetadataUtils.fromMd(md, TestClass.class);
    Assert.assertEquals("testId", testClass.id);
    Assert.assertEquals("testname", testClass.name);
    Assert.assertEquals(876, testClass.num);
    Assert.assertTrue(testClass.f);
  }

  @Test
  public void testFromMd2() {
    Metadata md = factory.createMetadata();
    md.setKey("testnamespace.testId");
    Mvalue mv0 = factory.createMvalue();
    mv0.setKey("number");
    mv0.setStringValueOrBinaryValue(factory.createMvalueStringValue("876"));
    Mvalue mv2 = factory.createMvalue();
    mv2.setKey("unused");
    mv2.setStringValueOrBinaryValue(factory.createMvalueStringValue("test"));
    md.getMvalue().addAll(Arrays.asList(mv0, mv2));

    TestClass testClass = MetadataUtils.fromMd(md, TestClass.class);
    Assert.assertEquals("testId", testClass.id);
    Assert.assertNull(testClass.name);
    Assert.assertEquals(876, testClass.num);
    Assert.assertFalse(testClass.f);
  }

  @Test
  public void testFromMdWrongNamespace() {
    try {
      Metadata md = factory.createMetadata();
      md.setKey("someothernamespace.testId");
      MetadataUtils.fromMd(md, TestClass.class);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("md key does not match with class namespace", e.getMessage());
    }
  }


}
