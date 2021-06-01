package eu.smesec.core.storage;

public class StorageImpl implements Storage {


  private PhysicalStorage storage;

  public StorageImpl(PhysicalStorage storage) {
    this.storage = storage;
  }

}
