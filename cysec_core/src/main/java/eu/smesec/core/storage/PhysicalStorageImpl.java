package eu.smesec.core.storage;

public class PhysicalStorageImpl implements PhysicalStorage {

  private String templateDirectory;
  private String dataDirectory;

  public PhysicalStorageImpl(String templateDirectory, String dataDirectory) {
    this.templateDirectory = templateDirectory;
    this.dataDirectory = dataDirectory;
  }

}
