/*-
 * #%L
 * CYSEC Platform Bridge
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
package eu.smesec.cysec.platform.bridge;

public enum Commands {
  LOAD_BLOCK,
  LOAD_QUESTION,
  ADD_RECOMMENDATION,
  REMOVE_RECOMMENDATION,
  UPDATE_ACTIVE_BLOCKS,
  UPDATE_AVAILABLE_BLOCKS,
  UPDATE_ACTIVE_QUESTIONS,
  SET_NEXT;

  @Override
  public String toString() {
    String[] parts = name().split("_");
    StringBuilder sb = new StringBuilder();

    // loop through all parts of the name
    for (int i = 0; i < parts.length; i++) {
      String word = parts[i];
      // use a lowercase letter for the first word
      if (i == 0) {
        sb.append(word.toLowerCase());
      // follow camel case pattern (first letter capital)
      } else {
        sb.append(String.valueOf(word.charAt(0)));
        sb.append(word.substring(1, word.length()).toLowerCase());
      }
    }
    return sb.toString();
  }
}
