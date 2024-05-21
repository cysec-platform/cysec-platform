/*-
 * #%L
 * CYSEC Platform Core
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
package eu.smesec.cysec.platform.core.messages;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

/**
 * GNU gettext message class. Used to handle the message strings for *.jsp files. All classes in
 * this package will be scanned for {@link I18n#tr(String)} and {@link I18n#trn(String, String,
 * long)} calls.
 */
public abstract class Message {
  protected final I18n i18n;
  protected final Map<String, String> messages;

  public Message(Locale locale) {
    i18n = I18nFactory.getI18n(getClass(), "Messages", locale != null ? locale : Locale.ENGLISH);
    messages = new HashMap<>();
  }

  /**
   * Returns all messages for the *.jsp file.
   *
   * @return dictionary
   */
  public Map<String, String> getMessages() {
    return messages;
  }
}
