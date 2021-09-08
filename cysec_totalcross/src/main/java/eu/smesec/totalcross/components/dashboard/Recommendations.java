package eu.smesec.totalcross.components.dashboard;

import eu.smesec.totalcross.components.common.iconButton.IconButton;
import eu.smesec.totalcross.components.common.iconButton.IconPosition;
import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.icon.MaterialIcons;
import totalcross.util.UnitsConverter;

public class Recommendations extends Container {

    @Override
    public void initUI() {
        int gap = UnitsConverter.toPixels(DP + 4);
        final int margin = UnitsConverter.toPixels(DP + 8);

        setBackColor(Color.brighter(0x215968));

        Container container = new Container() {
            @Override
            public void initUI() {
                setBackColor(0x215968);
                setInsets(margin, margin, margin, margin);
                int imageSize = UnitsConverter.toPixels(DP + 64);

                Label recLblSubTitle = new Label("Recommendation");
                recLblSubTitle.setForeColor(Color.WHITE);
                recLblSubTitle.setFont(Font.getFont("Lato Medium", false, recLblSubTitle.getFont().size));

                Label recLblTitle = new Label("Staff Training");
                recLblTitle.setForeColor(Color.WHITE);
                recLblTitle.setFont(Font.getFont("Lato Medium", true, recLblTitle.getFont().size + 8));

                Label recLblContent = new Label();
                recLblContent.setInsets(margin,margin,margin,margin);
                recLblContent.autoSplit = true;
                recLblContent.setForeColor(Color.WHITE);
                recLblContent.setFont(Font.getFont("Lato Medium", false, recLblContent.getFont().size));
                recLblContent.setText("Train your staff regularly with securityaware.me (if you do not provide cybersecurity training already)");

                add(recLblSubTitle, CENTER, TOP);
                add(recLblTitle, CENTER, AFTER + gap);
                add(recLblContent, CENTER, AFTER, PARENTSIZE, PREFERRED + gap);

                resizeHeight();
            }
        };
        IconButton btnLearnMore = new IconButton(MaterialIcons._ARROW_FORWARD, IconPosition.RIGHT, "Learn more");

        add(container, LEFT, TOP, FILL, PREFERRED);
        add(btnLearnMore, CENTER, AFTER, PARENTSIZE, PREFERRED);
        reposition();
        resizeHeight();
    }
}
