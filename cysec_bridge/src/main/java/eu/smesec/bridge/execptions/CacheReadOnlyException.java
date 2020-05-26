package eu.smesec.bridge.execptions;

public class CacheReadOnlyException extends CacheException {
  public CacheReadOnlyException(String message) {
    super(message);
  }
}
