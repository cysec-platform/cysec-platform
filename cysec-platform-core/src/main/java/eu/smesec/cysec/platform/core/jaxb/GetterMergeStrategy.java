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
package eu.smesec.cysec.platform.core.jaxb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jvnet.jaxb2_commons.lang.JAXBMergeStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

public class GetterMergeStrategy extends JAXBMergeStrategy {
  private final String getter;

  public GetterMergeStrategy(String getter) {
    this.getter = getter;
  }

  protected Object mergeInternal(
      ObjectLocator leftLocator,
      ObjectLocator rightLocator,
      Collection leftCollection,
      Collection rightCollection) {
    Collection<Object> merged = new ArrayList<>();
    Map<Object, Object> leftMap = new HashMap<>(leftCollection.size());
    try {
      // left lookup table
      for (Object item : leftCollection) {
        Class<?> clazz = item.getClass();
        Method m = clazz.getMethod(getter);
        leftMap.putIfAbsent(m.invoke(item), item);
      }
      // merge
      for (Object item : rightCollection) {
        Class<?> clazz = item.getClass();
        Method m = clazz.getMethod(getter);
        Object leftItem = leftMap.get(m.invoke(item));
        merged.add(leftItem != null ? merge(leftLocator, rightLocator, leftItem, item) : item);
      }
      return merged;
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
      // do nothing
    }
    return rightCollection;
  }
}
