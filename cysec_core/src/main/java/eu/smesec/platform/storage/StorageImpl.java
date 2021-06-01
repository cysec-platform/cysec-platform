package eu.smesec.platform.storage;

public class StorageImpl implements Storage {


  private PhysicalStorage storage;

  public StorageImpl(PhysicalStorage storage) {
    this.storage = storage;
  }

}
