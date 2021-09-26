package eu.smesec.cysec.platform.core.auth;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

@Singleton
public class SessionStore {
  private Map<String, Session> sessions = new HashMap<>();

  public Map<String, Session> getSessions() {
    return sessions;
  }

  public void startSession(String token, Session s) {
    sessions.put(token, s);
  }

  public void endSession(String token) {
    sessions.remove(token);
  }

  public boolean isActiveSession(String token) {
    return sessions.containsKey(token);
  }

  public SessionStore() {
    return;
  }
}
