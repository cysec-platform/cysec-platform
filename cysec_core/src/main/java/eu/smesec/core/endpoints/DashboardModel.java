package eu.smesec.core.endpoints;

import eu.smesec.bridge.FQCN;
import eu.smesec.bridge.generated.Questionnaire;
import eu.smesec.bridge.md.Badge;
import eu.smesec.bridge.md.Recommendation;
import eu.smesec.bridge.md.Skills;
import eu.smesec.bridge.utils.Tuple;

import eu.smesec.core.helpers.dashboard.CoachHelper;
import eu.smesec.core.messages.DashboardMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class DashboardModel {
    /**
     * Renders the questionnaire list for a company.
     *
     * @return The rendered html document.
     */

    public static Map<String, Object> getModel(Locale locale, Map<String, Questionnaire> allCoaches, List<CoachHelper> coachHelpers, List<Recommendation> recommendations, List<CoachHelper> remaining, List<Badge> badges, Tuple<FQCN, Skills> skills) {
        DashboardMsg msg =
                new DashboardMsg(locale, coachHelpers.size(), remaining.size(), badges.size());
        Map<String, Object> model = new HashMap<>();
        model.put("msg", msg.getMessages());
        model.put("instantiated", coachHelpers);
        model.put("remaining", remaining);
        model.put("recommendations", recommendations);
        model.put("badges", badges);
        if (skills != null) {
            model.put(
                    "skills",
                    new Tuple<>(getAnswerName(skills.getFirst(), allCoaches), skills.getSecond()));
        }
        return model;
    }

    private static String getAnswerName(FQCN fqcn, Map<String, Questionnaire> coaches) {
        StringBuilder name = new StringBuilder(coaches.get(fqcn.getCoachId()).getReadableName());
        String fileName = fqcn.getName();
        if (!fileName.equals("default")) {
            name.append(".").append(fileName);
        }
        return name.toString();
    }
}
