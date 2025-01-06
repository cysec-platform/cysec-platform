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
package eu.smesec.cysec.platform.core.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassFieldsExclusionStrategy implements ExclusionStrategy {
  private final Map<Class<?>, Set<String>> exclusion;

  public ClassFieldsExclusionStrategy() {
    this.exclusion = new HashMap<>();
  }

  public void ignoreClassFields(Class<?> clazz, String... fieldNames) {
    exclusion.put(clazz, new HashSet<>(Arrays.asList(fieldNames)));
  }

  @Override
  public boolean shouldSkipField(FieldAttributes f) {
    Set<String> exclusionFields = exclusion.get(f.getDeclaringClass());
    return exclusionFields != null && exclusionFields.contains(f.getName());
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
