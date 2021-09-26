package eu.smesec.cysec.platform.core.helpers.dashboard;

import eu.smesec.bridge.generated.Audit;

import java.util.Map;

public class AdminPageHelper {
  private final String configString;
  private final Map<String, Audit[]> auditMap;

  public AdminPageHelper(String configString, Map<String, Audit[]> auditMap) {
    this.configString = configString;
    this.auditMap = auditMap;
  }

  public String getConfigString() {
    return configString;
  }

  public Map<String, Audit[]> getAuditMap() {
    return auditMap;
  }
}
