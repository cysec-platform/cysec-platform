/*-
 * #%L
 * CYSEC Platform Core
 * %%
 * Copyright (C) 2020 - 2025 FHNW (University of Applied Sciences and Arts Northwestern Switzerland)
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

    messages.put("recommendations", i18n.trn("Recommendation", "Recommendations", 2L));
    messages.put("recommendation", i18n.tr("Recommendation"));
    messages.put("noRecommendation", i18n.tr("No recommendations available"));
    messages.put(
        "noRecommendationInfo",
        i18n.tr("If there are recommended next steps, they will be displayed here."));
    messages.put("coaches", i18n.trn("Coach", "Coaches", instantiated));
    messages.put("noCoachesStartedInfo", i18n.tr("No coaches started yet"));
    messages.put("coachRestart", i18n.tr("restart"));
    messages.put("coachContinue", i18n.tr("continue"));
    messages.put("coachReset", i18n.tr("reset"));
    messages.put("remaining", i18n.trn("Remaining coach", "Remaining coaches", remaining));
    messages.put("noRemaining", i18n.tr("All available coaches started."));
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
    messages.put("coachMore", i18n.tr("more"));
    messages.put("adminModalTitle", i18n.tr("Admin actions"));
    messages.put("adminModalExport", i18n.tr("Export"));
    messages.put("adminModalImport", i18n.tr("Import"));
  }
}
