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

import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;

public class FQCNTest {
  @Test
  public void testFromCoachId() {
    FQCN fqcn = FQCN.fromString("lib-company");
    Assert.assertEquals("lib-company.default", fqcn.toString());
  }

  @Test
  public void testFromTopLevel() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    Assert.assertEquals("lib-company.default", fqcn.toString());
  }

  @Test
  public void testFromString() {
    FQCN fqcn = FQCN.fromString("lib-company.sub-lib1.sub-lib3.A");
    Assert.assertEquals("lib-company.sub-lib1.sub-lib3.A", fqcn.toString());
  }

  @Test
  public void testFromCoachIdPath() {
    FQCN fqcn = FQCN.fromPath(Paths.get("lib-company"));
    Assert.assertEquals("lib-company.default", fqcn.toString());
  }

  @Test
  public void testFromTopLevelPath() {
    FQCN fqcn = FQCN.fromPath(Paths.get("lib-company/default.xml"));
    Assert.assertEquals("lib-company.default", fqcn.toString());
  }

  @Test
  public void testFromStringPath() {
    FQCN fqcn = FQCN.fromPath(Paths.get("lib-company/sub-lib1.sub-lib3/A.xml"));
    Assert.assertEquals("lib-company.sub-lib1.sub-lib3.A", fqcn.toString());
  }

  @Test
  public void testFromNull() {
    try {
      FQCN.fromString(null);
      Assert.fail();
    } catch (IllegalArgumentException ignored) {}
  }

  @Test
  public void testFromIllegalFormat() {
    try {
      FQCN.fromString(".test.default");
      Assert.fail();
    } catch (IllegalArgumentException ignored) {}
  }

  @Test
  public void testEquals() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    FQCN fqcn2 = FQCN.fromString("lib-company.default");
    Assert.assertEquals(fqcn, fqcn2);
  }

  @Test
  public void testNotEquals() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    FQCN fqcn2 = FQCN.fromString("lib-other.default");
    Assert.assertNotEquals(fqcn, fqcn2);
  }

  @Test
  public void testNotEquals2() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    Assert.assertNotEquals(fqcn, new Object());
  }

  @Test
  public void testGetRootCoachId() {
    FQCN fqcn = FQCN.fromString("lib-company.sub-lib1.sub-lib3.A");
    Assert.assertEquals("lib-company", fqcn.getRootCoachId());
  }

  @Test
  public void testGetRoot() {
    FQCN fqcn = FQCN.fromString("lib-company.sub-lib1.sub-lib3.A");
    FQCN root = FQCN.fromString("lib-company.default");
    Assert.assertEquals(root, fqcn.getRoot());
  }

  @Test
  public void testGetParentCoachId() {
    FQCN fqcn = FQCN.fromString("lib-company.sub-lib1.sub-lib3.A");
    Assert.assertEquals("sub-lib1", fqcn.getParentCoachId());
  }

  @Test
  public void testGetParentCoachNull() {
    FQCN fqcn = FQCN.fromString("lib-company");
    Assert.assertNull(fqcn.getParentCoachId());
  }

  @Test
  public void testGetParent() {
    FQCN fqcn = FQCN.fromString("lib-company.sub-lib1.sub-lib3.A");
    FQCN parent = FQCN.fromString("lib-company.sub-lib1.default");
    Assert.assertEquals(parent, fqcn.getParent());
  }

  @Test
  public void testGetNullParent() {
    FQCN fqcn = FQCN.fromString("lib-company");
    Assert.assertNull(fqcn.getParent());
  }

  @Test
  public void testGetCoachId() {
    FQCN fqcn = FQCN.fromString("lib-company.sub-lib1.sub-lib3.A");
    Assert.assertEquals("sub-lib3", fqcn.getCoachId());
  }

  @Test
  public void testGetCoachIdRoot() {
    FQCN fqcn = FQCN.fromString("lib-company");
    Assert.assertEquals("lib-company", fqcn.getCoachId());
  }

  @Test
  public void testResolve() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    FQCN resolved = FQCN.fromString("lib-company.sub-lib1.A");
    Assert.assertEquals(resolved, fqcn.resolve("A", "sub-lib1"));
  }

  @Test
  public void testResolve2() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    FQCN resolved = FQCN.fromString("lib-company.sub-lib1.sub-lib3.sub-libX.C");
    Assert.assertEquals(resolved, fqcn.resolve("C", "sub-lib1", "sub-lib3", "sub-libX"));
  }

  @Test
  public void testResolveDefault() {
    FQCN fqcn = FQCN.fromString("lib-company.default");
    FQCN resolved = FQCN.fromString("lib-company.sub-lib1.default");
    Assert.assertEquals(resolved, fqcn.resolveDefault("sub-lib1"));
  }


}
