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
package eu.smesec.cysec.platform.core.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ClassFieldsExclusionStrategyTest {
  private class Content {
    private String text;
    private int number;
    private long index;

    public Content(String text, int number, long index) {
      this.text = text;
      this.number = number;
      this.index = index;
    }
  }

  private class Wrapper {
    private Content content;
    private long index;

    public Wrapper(Content content, long index) {
      this.content = content;
      this.index = index;
    }
  }

  private ClassFieldsExclusionStrategy strategy;
  private Wrapper wrapper;

  @Before
  public void setup() {
    strategy = new ClassFieldsExclusionStrategy();
    wrapper = new Wrapper(new Content("sample", 4, 0L), 1L);
  }

  @Test
  public void testNoExclusion() {
    Gson gson = new Gson();
    Assert.assertEquals("{\"content\":{\"text\":\"sample\",\"number\":4,\"index\":0},\"index\":1}",
        gson.toJson(wrapper));
  }

  @Test
  public void testSingleExclusionContent() {
    strategy.ignoreClassFields(Content.class, "index");
    Gson gson = new GsonBuilder().addSerializationExclusionStrategy(strategy).create();
    Assert.assertEquals("{\"content\":{\"text\":\"sample\",\"number\":4},\"index\":1}",
        gson.toJson(wrapper));
  }

  @Test
  public void testSingleExclusionWrapper() {
    strategy.ignoreClassFields(Wrapper.class, "index");
    Gson gson = new GsonBuilder().addSerializationExclusionStrategy(strategy).create();
    Assert.assertEquals("{\"content\":{\"text\":\"sample\",\"number\":4,\"index\":0}}",
        gson.toJson(wrapper));
  }

  @Test
  public void testMultiExclusion() {
    strategy.ignoreClassFields(Content.class, "number");
    strategy.ignoreClassFields(Wrapper.class, "index");
    Gson gson = new GsonBuilder().addSerializationExclusionStrategy(strategy).create();
    Assert.assertEquals("{\"content\":{\"text\":\"sample\",\"index\":0}}",
        gson.toJson(wrapper));
  }
}
