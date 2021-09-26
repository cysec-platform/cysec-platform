package eu.smesec.cysec.platform.bridge.md;

import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;

@MdNamespace(MetadataUtils.MD_LAST_SELECTED)
public class LastSelected {
  @MvKey(MetadataUtils.MV_FQCN)
  protected String coachId;

  public LastSelected() {}

  public LastSelected(String coachId) {
    this.coachId = coachId;
  }

  public String getCoachId() {
    return coachId;
  }

  public void setCoachId(String coachId) {
    this.coachId = coachId;
  }
}
