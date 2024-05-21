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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.PropertyObjectLocator;

public class ClassFieldCopyStrategy extends JAXBCopyStrategy {
  public static final JAXBCopyStrategy INSTANCE = new JAXBCopyStrategy();

  private Map<Class<?>, Set<String>> fields;

  public ClassFieldCopyStrategy() {
    this.fields = new HashMap<>();
  }

  public void addFields(Class<?> clazz, String... fields) {
    this.fields.putIfAbsent(clazz, Arrays.stream(fields).collect(Collectors.toSet()));
  }

  protected Object copyInternal(ObjectLocator locator, Object object) {
    if (locator instanceof PropertyObjectLocator) {
      PropertyObjectLocator pol = (PropertyObjectLocator) locator;
      Class<?> type = pol.getObject().getClass();
      String fieldName = pol.getPropertyName();
      Set<String> enabledFields = fields.get(type);
      if (enabledFields != null && enabledFields.contains(fieldName)) {
        return super.copyInternal(locator, object);
      }
    }
    return null;
  }
}
