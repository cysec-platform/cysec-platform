package eu.smesec.bridge.execptions;

import eu.smesec.bridge.generated.Library;

public class LibraryException extends Exception {
  private Library lib;

  public LibraryException(Library lib,  String msg) {
    super(msg);
    this.lib = lib;
  }

  public Library getLib() {
    return lib;
  }
}
