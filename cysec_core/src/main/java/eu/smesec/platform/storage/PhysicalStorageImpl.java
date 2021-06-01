package eu.smesec.platform.storage;

public class PhysicalStorageImpl implements PhysicalStorage {

  private String templateDirectory;
  private String dataDirectory;

  public PhysicalStorageImpl(String templateDirectory, String dataDirectory) {
    this.templateDirectory = templateDirectory;
    this.dataDirectory = dataDirectory;
  }

}
