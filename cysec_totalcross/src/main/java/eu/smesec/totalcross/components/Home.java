package eu.smesec.totalcross.components;

import eu.smesec.totalcross.components.dashboard.Coaches;
import eu.smesec.totalcross.components.dashboard.Recommendations;
import totalcross.sys.Settings;
import totalcross.ui.Label;
import totalcross.ui.ScrollContainer;
import totalcross.ui.font.Font;

import java.util.Locale;

public class Home extends BaseScreen {
    private final int gap = (int) (Settings.screenDensity * 20);

    String[] mockCoaches = {"Company", "Malware.lib", "Patch Management", "Access Control", "Backup Coach.A"};

    public Home() {

    }

    @Override
    public void onContent(ScrollContainer content) {
        final int DP_10 = (int) (Settings.screenDensity * 10);
        content.setInsets(0, 0, DP_10, DP_10);
        Label recommendationsLbl = new Label("Recommendations".toUpperCase(Locale.ROOT));
        recommendationsLbl.setFont(Font.getFont("Lato Medium", true, recommendationsLbl.getFont().size + 6));
        content.add(recommendationsLbl, LEFT + gap, TOP + gap, PREFERRED, PREFERRED);
        content.add(new Recommendations(), LEFT + gap, AFTER, FILL - gap, PREFERRED);
        content.add(new Recommendations(), LEFT + gap, AFTER, FILL - gap, PREFERRED);
        content.add(new Recommendations(), LEFT + gap, AFTER, FILL - gap, PREFERRED);
        Label coachesLbl = new Label("Coaches".toUpperCase(Locale.ROOT));
        coachesLbl.setFont(Font.getFont("Lato Medium", true, coachesLbl.getFont().size + 6));
        content.add(coachesLbl, LEFT + gap, AFTER + gap, PREFERRED, PREFERRED);
        for (String coach : mockCoaches) {
            content.add(new Coaches(coach), LEFT + gap, AFTER, FILL - gap, PREFERRED);
        }
        Label remainingCoachesLbl = new Label("Remaining Coaches".toUpperCase(Locale.ROOT));
        remainingCoachesLbl.setFont(Font.getFont("Lato Medium", true, remainingCoachesLbl.getFont().size + 6));
        content.add(remainingCoachesLbl, LEFT + gap, AFTER + gap, PREFERRED, PREFERRED);
//        content.add(new RemainingCoachList(), LEFT + gap, AFTER + space, FILL - gap, 170 + DP);
    }
}
