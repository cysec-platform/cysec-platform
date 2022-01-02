/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.auth;

/** Enum representing all valid Crypt types to be used in CryptPasswordStorage. */
public enum CryptType {
  MD5("1"),
  SHA256("5"),
  SHA512("6"),
  PLAIN("99");

  private String id;

  CryptType(String id) {
    this.id = id;
  }

  /**
   * Returns the ID representing the crypt type in Crypt(3).
   *
   * @return The string ID
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the default CryptType to be used if not speciied.
   *
   * @return The default CryptType
   */
  public static CryptType getDefault() {
    return SHA512;
  }

  /**
   * Look up an algorithm by id.
   *
   * @param id the idto be looked up
   * @return the algorithm or null if not known
   */
  public static CryptType getById(String id) {
    for (CryptType e : values()) {
      if (e.id.equals(id)) {
        return e;
      }
    }
    return null;
  }
}
