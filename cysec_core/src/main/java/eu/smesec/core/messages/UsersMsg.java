package eu.smesec.core.messages;

import java.util.Locale;

public class UsersMsg extends Message {
  /**
   * Coach page messages constructor.
   *
   * @param locale display language
   * @param users number of users
   */
  public UsersMsg(Locale locale, int users) {
    super(locale);

    messages.put("users", i18n.trn("User", "Users", users));
    messages.put("noUsers", i18n.tr("no users loaded."));
    messages.put("addUser", i18n.tr("add user"));
    messages.put("editUser", i18n.tr("edit user"));
    messages.put("selectLocale", i18n.tr("select language"));
    messages.put(
        "info",
        i18n.tr("maybe some general information on how to obtain a new user or reset password?"));
    messages.put("add", i18n.tr("add"));
    messages.put("edit", i18n.tr("edit"));

    messages.put("username", i18n.tr("username"));
    messages.put("firstname", i18n.tr("first name"));
    messages.put("surname", i18n.tr("surname"));
    messages.put("password", i18n.tr("password"));
    messages.put("email", i18n.tr("e-mail"));
    messages.put("locale", i18n.tr("language"));
    messages.put("roles", i18n.tr("roles"));
    messages.put("lock", i18n.tr("lock"));

    messages.put("token_header", i18n.tr("Replica Token"));
    messages.put(
        "token_empty", i18n.tr("no replica token defined, please contact your server admin"));
  }
}
