package eu.smesec.bridge.execptions;

public class CacheException extends Exception {
//  public static final int GENERAL_ERROR = 1;
//  public static final int IO_ERROR = 10;
//  public static final int JAXB_ERROR = 20;
//  public static final int UNKOWN_COMMAND = 50;
//
//  public static final int COMPANY_ERROR = 100;
//  public static final int COMPANY_NOT_FOUND = 104;
//  public static final int COMPANY_ALREADY_EXISTS = 105;
//
//  public static final int USER_ERROR = 200;
//  public static final int USER_NOT_FOUND = 204;
//  public static final int USER_ALREADY_EXISTS = 205;

//  private int code;

  public CacheException(/* int code, */ String message) {
    super(message);
//    this.code = code;
  }

//  public int getCode() {
//    return code;
//  }
}
