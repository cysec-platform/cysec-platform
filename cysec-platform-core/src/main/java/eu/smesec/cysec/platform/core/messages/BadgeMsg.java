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
