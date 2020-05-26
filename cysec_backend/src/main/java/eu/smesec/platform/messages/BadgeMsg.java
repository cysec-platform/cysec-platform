package eu.smesec.platform.messages;

import java.util.Locale;

public class BadgeMsg extends Message {
  public BadgeMsg(Locale locale, int badges) {
    super(locale);

    messages.put("title", i18n.tr("All badges"));
    messages.put("unlocked", i18n.trn("You have unlocked fallowing achievement", "You have unlocked fallowing achievements", badges));
  }
}
