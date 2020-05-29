package eu.smesec.platform.auth;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.digest.Crypt;

/** Password storage implementation for linux type crypt passwords. */
public class CryptPasswordStorage extends PasswordStorage {
  /* static random number generator for efficiency */
  static final SecureRandom random = new SecureRandom();

  /**
   * Initializes a new password storage with the given password and salt.
   *
   * @param password The password to be stored
   * @param salt The salt to be used. If salt is null a random salt will be generated
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  public CryptPasswordStorage(String password, String salt) throws NoSuchAlgorithmException {
    this(password, salt, CryptType.getDefault());
  }

  /**
   * Initializes a new password storage with the given password and salt.
   *
   * @param password The password to be stored
   * @param salt The salt to be used. If salt is null a random salt will be gennerated
   * @param type The crypt type to be used
   * @throws NoSuchAlgorithmException if default CryptType is illegal and type is null
   * @throws IllegalArgumentException if salt is an empty String
   */
  public CryptPasswordStorage(String password, String salt, CryptType type)
      throws NoSuchAlgorithmException {
    setPassword(password, salt, type);
  }

  /**
   * Initializes a new password storage with the given storage.
   *
   * @param storageString the hashed representation of the password
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  public CryptPasswordStorage(String storageString) throws NoSuchAlgorithmException {
    super(storageString);
    if (getType() == null) {
      throw new IllegalArgumentException("crypt type is unknown");
    }
  }

  /**
   * Sets a new password.
   *
   * @param password The password to be stored
   * @param salt The salt to be used. If salt is null a random salt will be gennerated
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  @Override
  void setPassword(String password, String salt) throws NoSuchAlgorithmException {
    setPassword(password, salt, getType());
  }

  /**
   * Sets a new password.
   *
   * @param password The password to be stored
   * @param salt The salt to be used. If salt is null a random salt will be gennerated
   * @param type The crypt type to be used
   * @throws NoSuchAlgorithmException if default CryptType is illegal
   * @throws IllegalArgumentException if salt is an empty String
   */
  void setPassword(String password, String salt, CryptType type) throws NoSuchAlgorithmException {
    // TODO: handle type CRYPT.PLAIN differently
    if (salt == null) {
      salt = getRandomHexString();
    }
    if ("".equals(salt)) {
      throw new IllegalArgumentException("salt may not be empty");
    }
    if (type == null) {
      type = CryptType.getDefault();
    }
    if (type == null) {
      throw new NoSuchAlgorithmException("default CryptType is invalid");
    }
    if (type.equals(CryptType.PLAIN)) {
      storage = String.format("$%s$%s$%s", type.getId(), salt, password);
    } else {
      storage = Crypt.crypt(password, "$" + type.getId() + "$" + salt + "$");
    }
  }

  /**
   * Gets the password salt.
   *
   * @return String repreentation of the password salt
   */
  public String getSalt() {
    String[] splittedStorage = storage.split("\\$");
    if (splittedStorage.length != 4) {
      throw new IllegalArgumentException("storage does not satisfy requirements");
    }
    return splittedStorage[2];
  }

  /**
   * Gets the current crypt type.
   *
   * @return The crypt type or null if an unknown crypt type is represented in the storage
   */
  public CryptType getType() {
    if (storage == null) {
      return null;
    }
    String[] splittedStorage = storage.split("\\$");
    if (splittedStorage.length < 2) {
      return null;
    }
    return CryptType.getById(splittedStorage[1]);
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
  @Override
  public boolean verify(String password) throws NoSuchAlgorithmException {
    CryptPasswordStorage t = new CryptPasswordStorage(password, getSalt(), getType());
    return t.equals(this);
  }

  private static String getRandomHexString() {
    StringBuilder sb = new StringBuilder();
    while (sb.length() < 32) {
      sb.append(Integer.toHexString(random.nextInt()));
    }

    return sb.toString().substring(0, 32);
  }
}
