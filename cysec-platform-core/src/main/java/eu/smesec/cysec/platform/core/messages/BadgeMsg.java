/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2022 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

public class BadgeMsg extends Message {
  /**
   * Badge overview page messages constructor.
   *
   * @param locale display language
   * @param badges number of badges
   */
  public BadgeMsg(Locale locale, int badges) {
    super(locale);

    messages.put("title", i18n.tr("All badges"));
    messages.put(
        "unlocked",
        i18n.trn(
            "You have unlocked fallowing achievement",
            "You have unlocked fallowing achievements",
            badges));
  }
}
