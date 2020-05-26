package eu.smesec.bridge.execptions;

public class MissingConfigException extends ConfigException {
  private String id;

  public MissingConfigException(String id, String message) {
    super(message);
  }

  public String getId() {
    return id;
  }
}
