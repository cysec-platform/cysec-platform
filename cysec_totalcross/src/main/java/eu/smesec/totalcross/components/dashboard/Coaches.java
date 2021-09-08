package eu.smesec.totalcross.components.dashboard;

import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.util.UnitsConverter;

import java.util.Locale;

public class Coaches extends Container {

    private final String coach;

    public Coaches(String coach) {
        this.coach = coach;
    }

    @Override
    public void initUI() {
        int gap = UnitsConverter.toPixels(DP + 4);
        final int margin = UnitsConverter.toPixels(DP + 8);

        setBackColor(0xFFFFFF);
//        setBorderStyle(BORDER_ROUNDED);

        Container leftContainer = new Container() {
            @Override
            public void initUI() {
                setBackColor(0xd3d6de);
                setInsets(margin, margin, margin, margin);
                int imageSize = UnitsConverter.toPixels(DP + 64);

                Label coachTitleLbl = new Label(coach.toUpperCase(Locale.ROOT));
                coachTitleLbl.setFont(Font.getFont("Lato Medium", false, coachTitleLbl.getFont().size + 6));

                Label scoreValueLbl = new Label("3.0");
                scoreValueLbl.setFont(Font.getFont("Lato Medium", true, scoreValueLbl.getFont().size + 6));

                Label scoreLbl = new Label();
                scoreLbl.autoSplit = true;
                scoreLbl.setFont(Font.getFont("Lato Medium", false, scoreLbl.getFont().size));
                scoreLbl.setText("Score");

                Button restartBtn = new Button("RESTART", Button.BORDER_NONE);
                restartBtn.setBackForeColors(0x215968, Color.WHITE);
                Button continueBtn = new Button("CONTINUE", Button.BORDER_NONE);
                continueBtn.setBackForeColors(Color.brighter(0x215968), Color.WHITE);

                add(coachTitleLbl, LEFT, TOP);
                add(restartBtn, LEFT, AFTER + 2 * gap, restartBtn.getPreferredWidth() <= 24 ? DP + 48 : restartBtn.getPreferredWidth(), DP + 27);
                add(continueBtn, AFTER + gap, SAME, continueBtn.getPreferredWidth() <= 24 ? DP + 48 : continueBtn.getPreferredWidth(), DP + 27);
                add(scoreValueLbl, RIGHT, TOP);
                add(scoreLbl, RIGHT, AFTER + gap);

                resizeHeight();
            }
        };

        add(leftContainer, LEFT, TOP, PARENTSIZE, PREFERRED);
        reposition();
        resizeHeight();
    }

}
