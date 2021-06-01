package eu.smesec.core;

public class Version {
  private static final String BUILD_VERSION = Version.class.getPackage().getImplementationVersion();
  private static final String VERSION = Version.class.getPackage().getSpecificationVersion();

  public static String getVersion() {
    return VERSION;
  }

  public static String getBuildVersion() {
    return BUILD_VERSION;
  }

  public static void main(String[] args) {
    System.out.println("VERSION=" + getVersion());
    System.out.println("BUILD=" + getBuildVersion());
  }
}
