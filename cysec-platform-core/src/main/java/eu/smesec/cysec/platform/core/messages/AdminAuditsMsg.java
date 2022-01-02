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

public class AdminAuditsMsg extends Message {
  /**
   * Admin audit page messages constructor.
   *
   * @param locale display language
   * @param audits number of audits
   */
  public AdminAuditsMsg(Locale locale, int audits) {
    super(locale);

    messages.put("audits", i18n.trn("audit", "audits", audits));
    messages.put("headerTime", i18n.tr("time"));
    messages.put("headerUser", i18n.tr("user"));
    messages.put("headerAction", i18n.tr("action"));
    messages.put("headerBefore", i18n.tr("before"));
    messages.put("headerAfter", i18n.tr("after"));
    messages.put("noAudits", i18n.tr("No audits found"));
  }
}
