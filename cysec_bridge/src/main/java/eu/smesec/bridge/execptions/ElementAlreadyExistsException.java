package eu.smesec.bridge.execptions;

public class ElementAlreadyExistsException extends CacheException {
  public ElementAlreadyExistsException(String message) {
    super(message);
  }
}
