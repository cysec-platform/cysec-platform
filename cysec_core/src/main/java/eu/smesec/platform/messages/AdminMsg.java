package eu.smesec.platform.messages;

import java.util.Locale;

public class AdminMsg extends Message {
  /**
   * Admin overview page messages constructor.
   *
   * @param locale display language
   * @param companies number of companies
   */
  public AdminMsg(Locale locale, int companies) {
    super(locale);

    messages.put("title", i18n.tr("Admin Page"));
    messages.put("companies", i18n.trn("Installed company", "Installed companies", companies));
    messages.put("noCompanies", i18n.tr("No companies installed"));
  }
}