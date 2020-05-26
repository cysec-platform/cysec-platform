package eu.smesec.bridge.md;

import eu.smesec.bridge.md.annotations.MdNamespace;
import eu.smesec.bridge.md.annotations.MvKey;

@MdNamespace(MetadataUtils.MD_RESUME)
public class Resume {
  @MvKey(MetadataUtils.MV_Q_ID)
  protected String qid;

  public String getQid() {
    return qid;
  }

  public void setQid(String qid) {
    this.qid = qid;
  }
}
