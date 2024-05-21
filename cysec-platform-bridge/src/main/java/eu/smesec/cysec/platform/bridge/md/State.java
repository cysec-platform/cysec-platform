/*-
 * #%L
 * CYSEC Platform Bridge
 * %%
 * Copyright (C) 2020 - 2024 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
