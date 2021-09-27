package eu.smesec.cysec.platform.bridge.md;

import eu.smesec.cysec.platform.bridge.md.annotations.MdNamespace;
import eu.smesec.cysec.platform.bridge.md.annotations.MvKey;

@MdNamespace("_cysec.state")
public class State {
  @MvKey("current")
  protected String resume;

  @MvKey("active")
  protected String active;

  public State() {}

  public State(String resume, String activeQuestions) {
    this.resume = resume;
    this.active = activeQuestions;
  }

  public String getResume() {
    return resume;
  }

  public void setResume(String resume) {
    this.resume = resume;
  }

  public String getActive() {
    return active;
  }

  public void setActive(String active) {
    this.active = active;
  }
}
