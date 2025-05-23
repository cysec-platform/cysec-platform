/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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
package eu.smesec.cysec.platform.core.messages;

import java.util.Locale;

public class CoachMsg extends Message {
  /**
   * Coach page messages constructor.
   *
   * @param locale display language
   */
  public CoachMsg(Locale locale) {
    super(locale);

    messages.put("readmore", i18n.tr("read more"));
    messages.put("next", i18n.tr("next"));
    messages.put("summary", i18n.tr("Summary"));
    messages.put("unflagQuestion", i18n.tr("Remove the flag on this question"));
    messages.put("flagQuestion", i18n.tr("Flag this question for later"));
  }
}
