package eu.smesec.platform.messages;

import java.util.Locale;

public class CoachMsg extends Message {
  public CoachMsg(Locale locale) {
    super(locale);

    messages.put("readmore", i18n.tr("read more"));
    messages.put("next", i18n.tr("next"));
  }
}
