package eu.smesec.cysec.platform.core.auth;

public class Session {

  private String username;

  // Could contain Responses given by user, questionQueue for user,
  // call libraries, reference to active questionnaire etc.

  public Session(String username) {
    this.username = username;
  }
}
