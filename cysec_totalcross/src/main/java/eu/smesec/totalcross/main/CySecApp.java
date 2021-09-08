package eu.smesec.totalcross.main;

import eu.smesec.totalcross.components.Home;
import eu.smesec.totalcross.util.Colors;
import totalcross.io.IOException;
import totalcross.sys.Settings;
import totalcross.ui.*;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.icon.MaterialIcons;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.UnitsConverter;

public class CySecApp extends MainWindow {

    SideMenuContainer sideMenu;

    public CySecApp() {
        super("CySec", NO_BORDER);
        setUIStyle(Settings.MATERIAL_UI);
        uiAdjustmentsBasedOnFontHeightIsSupported = false;
        setBackForeColors(Colors.BACKGROUND, Colors.SURFACE);
    }

    @Override
    public void initUI() {

        SideMenuContainer.Item home = new SideMenuContainer.Item("Home", MaterialIcons._HOME, Color.BLACK, false, Home::new);

        sideMenu = new SideMenuContainer(null,
                home
        );

        sideMenu.topMenu.header = new Container() {
            @Override
            public void initUI() {
                try {
                    setBackColor(0x1c4dbd);

                    Label title = new Label("CySec", LEFT, Color.WHITE, false);
                    title.setFont(Font.getFont("Lato Bold", false, this.getFont().size + 5));
                    title.setForeColor(Color.WHITE);
                    add(title, LEFT + UnitsConverter.toPixels(Control.DP + 10), BOTTOM - UnitsConverter.toPixels(Control.DP + 10), FILL, DP + 56);

                    ImageControl profile = new ImageControl(new Image("images/recommendation_bulb.png"));
                    profile.centerImage = true;
                    profile.hwScale = true;
                    profile.scaleToFit = true;
                    profile.transparentBackground = true;
                    add(profile, LEFT + UnitsConverter.toPixels(Control.DP + 10), UnitsConverter.toPixels(Control.DP + 25), PREFERRED, FIT);

                } catch (IOException | ImageException e) {
                    e.printStackTrace();
                }
            }
        };

        sideMenu.setBarFont(Font.getFont(Font.getDefaultFontSize() + 3));
        sideMenu.setBackColor(0x1c4dbd);
        sideMenu.setForeColor(Color.WHITE);
        sideMenu.setItemForeColor(Color.BLACK);
        sideMenu.topMenu.drawSeparators = false;
        sideMenu.topMenu.itemHeightFactor = 3;

        add(sideMenu, LEFT, TOP, PARENTSIZE, PARENTSIZE);
    }
}
