package eu.smesec.platform.messages;

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
