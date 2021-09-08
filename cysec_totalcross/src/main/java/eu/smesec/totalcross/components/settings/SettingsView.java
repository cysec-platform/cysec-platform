package eu.smesec.totalcross.components.settings;

import totalcross.sys.Settings;
import totalcross.ui.AlignedLabelsContainer;
import totalcross.ui.Container;
import totalcross.ui.Label;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.util.UnitsConverter;

public class SettingsView extends Container {

    @Override
    public void initUI() {
        AlignedLabelsContainer alc =
                new AlignedLabelsContainer(
                        new String[]{
                                "Application version",
                                "TotalCross version",
                                "OS",
                                "OS version",
                                "Screen density",
                                "Device font size",
                                "Actual font size",
                                "Width x Height"
                        });
        add(alc, LEFT, TOP, FILL, FILL);
        int padding = (int) (Settings.screenDensity * 8);
        alc.setInsets(padding, padding, padding, padding);
        alc.setForeColor(Color.BLACK);
        int lineY = 0;
        alc.add(new Label("Settings.appVersion"), LEFT + UnitsConverter.toPixels(DP + 15), alc.getLineY(lineY++));
        alc.add(new Label("Settings.versionStr"), LEFT + UnitsConverter.toPixels(DP + 15), alc.getLineY(lineY++));
        alc.add(new Label("Settings.platform"), LEFT + UnitsConverter.toPixels(DP + 15), alc.getLineY(lineY++));
        alc.add(
                new Label(String.valueOf(Settings.romVersion)),
                LEFT + UnitsConverter.toPixels(DP + 15),
                alc.getLineY(lineY++));
        alc.add(
                new Label(String.valueOf(Settings.screenDensity)),
                LEFT + UnitsConverter.toPixels(DP + 15),
                alc.getLineY(lineY++));
        alc.add(
                new Label(String.valueOf(Settings.deviceFontHeight)),
                LEFT + UnitsConverter.toPixels(DP + 15),
                alc.getLineY(lineY++));
        alc.add(
                new Label(String.valueOf(Font.getDefaultFontSize())),
                LEFT + UnitsConverter.toPixels(DP + 15),
                alc.getLineY(lineY++));
        alc.add(
                new Label(String.valueOf(Settings.screenWidth) + "x" + String.valueOf(Settings.screenHeight)),
                LEFT + UnitsConverter.toPixels(DP + 15),
                alc.getLineY(lineY));

    }
}