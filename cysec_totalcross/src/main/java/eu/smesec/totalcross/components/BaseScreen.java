package eu.smesec.totalcross.components;

import eu.smesec.totalcross.components.common.iconButton.IconButton;
import eu.smesec.totalcross.components.common.iconButton.IconPosition;
import eu.smesec.totalcross.components.settings.SettingsView;
import eu.smesec.totalcross.components.skills.SkillsView;
import totalcross.ui.Container;
import totalcross.ui.MaterialWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.icon.MaterialIcons;

public abstract class BaseScreen extends Container {

    protected ScrollContainer content;
    protected boolean allowVerticalScroll = true;
    protected boolean allowHorizontalScroll = false;
    Container bar;


    @Override
    public void initUI() {
        bar = new Container();
        bar.setBackColor(0x1c4dbd);

        IconButton skillsBtn = new IconButton(MaterialIcons._BUBBLE_CHART, IconPosition.RIGHT, "Company Skills");
        skillsBtn.addPressListener(c -> {
            new MaterialWindow("Company Skills", false, SkillsView::new).popup();
        });


        IconButton settingsBtn = new IconButton(MaterialIcons._SETTINGS, IconPosition.RIGHT, "Settings");
        settingsBtn.addPressListener(c -> {
            new MaterialWindow("Settings", false, SettingsView::new).popup();
        });


        add(bar, LEFT, TOP, PARENTSIZE, DP + 52);
        bar.add(skillsBtn, LEFT, TOP, PARENTSIZE + 50, PREFERRED);
        bar.add(settingsBtn, RIGHT, TOP, PARENTSIZE + 50, PREFERRED);
        bar.resizeHeight();

        content = new ScrollContainer() {
            @Override
            public void initUI() {
                setScrollBars(allowHorizontalScroll, allowVerticalScroll);
                super.initUI();
            }
        };
        // Content
        add(content, LEFT, AFTER, PARENTSIZE, FILL);
        onContent(content);
    }

    public abstract void onContent(ScrollContainer content);


}
