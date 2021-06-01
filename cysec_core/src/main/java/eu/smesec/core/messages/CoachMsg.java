package eu.smesec.core.messages;

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
  }
}
