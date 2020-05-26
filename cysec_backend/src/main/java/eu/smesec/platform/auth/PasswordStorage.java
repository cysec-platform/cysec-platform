package eu.smesec.platform.auth;

import java.security.NoSuchAlgorithmException;

/**
 * <p>Abstract Password storage container.</p>
 */
public abstract class PasswordStorage {

  String storage = null;

  protected PasswordStorage() {}

  /***
   * <p>Initializes a new password storage with the given password and salt.</p>
   *
   * @param password  The password to be stored
   * @param salt      The salt to be used. If salt is null a random salt will be gennerated
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  public PasswordStorage(String password, String salt) throws NoSuchAlgorithmException {
    setPassword(password, salt);
  }

  /***
   * <p>Initializes a new password storage with the given storage.</p>
   *
   * @param storageString             the storage representation of the password
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  public PasswordStorage(String storageString) throws NoSuchAlgorithmException {
    setStorage(storageString);
  }

  /***
   * <p>Sets a new password.</p>
   *
   * @param password  The password to be stored
   * @param salt      The salt to be used. If salt is null a random salt will be gennerated
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  abstract void setPassword(String password, String salt) throws NoSuchAlgorithmException;

  /***
   * <p>Sets a new storage.</p>
   *
   * @param storage   The storage string
   * @throws NoSuchAlgorithmException if CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  void setStorage(String storage) throws NoSuchAlgorithmException {
    this.storage = storage;
    verify("");
  }

  /***
   * <p>Returns the storage string.</p>
   *
   * @return the storage string
   */
  public String getPasswordStorage() {
    return storage;
  }

  /***
   * <p>Verifies a given plain text password against the storage.</p>
   *
   * @param password      The password to be verified
   * @return              True if password matches
   * @throws NoSuchAlgorithmException if the storage does not contain a storage
   *                                  with a valid CRYPT-TYPE
   * @throws IllegalArgumentException if the salt string in the storage is empty
   */
  public abstract boolean verify(String password) throws NoSuchAlgorithmException;

  @Override
  public boolean equals(Object o) {
    return (o instanceof PasswordStorage) && (storage != null)
            && (storage.equals(((PasswordStorage)(o)).storage));
  }

  @Override
  public int hashCode() {
    return storage.hashCode();
  }
}
