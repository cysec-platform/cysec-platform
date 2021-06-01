package eu.smesec.core.messages;

import java.util.Locale;

public class DashboardMsg extends Message {
  /**
   * Dashboard page messages constructor.
   *
   * @param locale display language
   * @param instantiated number of instantiated coaches
   * @param remaining number of remaining coaches
   * @param badges number of badges
   */
  public DashboardMsg(Locale locale, int instantiated, int remaining, int badges) {
    super(locale);

    messages.put("recommendations", i18n.trn("recommendation", "recommendations", 2L));
    messages.put("recommendation", i18n.tr("recommendation"));
    messages.put("noRecommendation", i18n.tr("no recommendations to display"));
    messages.put(
        "noRecommendationInfo",
        i18n.tr("Later you will see recommended next steps displayed here"));
    messages.put("coaches", i18n.trn("coach", "coaches", instantiated));
    messages.put("coachRestart", i18n.tr("restart"));
    messages.put("coachContinue", i18n.tr("continue"));
    messages.put("remaining", i18n.trn("remaining coach", "remaining coaches", remaining));
    messages.put("noRemaining", i18n.tr("All coaches done"));
    messages.put("skills", i18n.tr("skills"));
    messages.put("strength", i18n.tr("strength"));
    messages.put("knowHow", i18n.tr("know-how"));
    messages.put("fitness", i18n.tr("fitness"));
    messages.put("achievedLevels", i18n.trn("level achieved", "levels achieved", instantiated));
    messages.put("noLevels", i18n.tr("No grades available"));
    messages.put(
        "latestAchievements", i18n.trn("latest achievement", "latest achievements", badges));
    messages.put("noAchievements", i18n.tr("No achievements have been unlocked"));
    messages.put("showAll", i18n.tr("show all"));
  }
}
