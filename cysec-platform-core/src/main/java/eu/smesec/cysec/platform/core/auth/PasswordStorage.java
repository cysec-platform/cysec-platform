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
package eu.smesec.cysec.platform.core.auth;

import java.security.NoSuchAlgorithmException;

/** Abstract Password storage container. */
public abstract class PasswordStorage {

  String storage = null;

  protected PasswordStorage() {}

  /**
   * Initializes a new password storage with the given password and salt.
   *
   * @param password The password to be stored
   * @param salt The salt to be used. If salt is null a random salt will be gennerated
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  public PasswordStorage(String password, String salt) throws NoSuchAlgorithmException {
    setPassword(password, salt);
  }

  /**
   * Initializes a new password storage with the given storage.
   *
   * @param storageString the storage representation of the password
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  public PasswordStorage(String storageString) throws NoSuchAlgorithmException {
    setStorage(storageString);
  }

  /**
   * Sets a new password.
   *
   * @param password The password to be stored
   * @param salt The salt to be used. If salt is null a random salt will be gennerated
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  abstract void setPassword(String password, String salt) throws NoSuchAlgorithmException;

  /**
   * Sets a new storage.
   *
   * @param storage The storage string
   * @throws NoSuchAlgorithmException if CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  void setStorage(String storage) throws NoSuchAlgorithmException {
    this.storage = storage;
    verify("");
  }

  /**
   * Returns the storage string.
   *
   * @return the storage string
   */
  public String getPasswordStorage() {
    return storage;
  }

  /**
   * Verifies a given plain text password against the storage.
   *
   * @param password The password to be verified
   * @return True if password matches
   * @throws NoSuchAlgorithmException if the storage does not contain a storage with a valid
   *     CRYPT-TYPE
   * @throws IllegalArgumentException if the salt string in the storage is empty
   */
  public abstract boolean verify(String password) throws NoSuchAlgorithmException;

  @Override
  public boolean equals(Object o) {
    return (o instanceof PasswordStorage)
        && (storage != null)
        && (storage.equals(((PasswordStorage) (o)).storage));
  }

  @Override
  public int hashCode() {
    return storage.hashCode();
  }
}
