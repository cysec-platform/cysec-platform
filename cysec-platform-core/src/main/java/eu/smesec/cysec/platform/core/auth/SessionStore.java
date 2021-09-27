/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2021 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
