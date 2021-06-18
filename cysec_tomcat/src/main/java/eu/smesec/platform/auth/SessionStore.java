package eu.smesec.platform.auth;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

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
