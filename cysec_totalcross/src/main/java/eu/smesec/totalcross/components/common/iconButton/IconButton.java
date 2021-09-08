package eu.smesec.totalcross.components.common.iconButton;

import eu.smesec.totalcross.util.Colors;
import totalcross.ui.Button;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.icon.IconType;
import totalcross.util.UnitsConverter;

public class IconButton extends Button {
    IconType iconType;
    final int UNDEFINED = -999999;
    int textX, iconX, iconY; // centralized text
    String originalText;
    IconPosition iconPosition = IconPosition.LEFT;


    public IconButton(IconType iconType, String text) {
        super(text);
        originalText = text;
        backColor = Colors.PRIMARY;
        transparentBackground = true;
        setNinePatch(null);
        this.iconType = iconType;
        textX = iconX = iconY = UNDEFINED;
    }

    public IconButton(IconType iconType, IconPosition position, String text) {
        this(iconType, text);
        this.iconPosition = position;
    }

    @Override
    protected void onBoundsChanged(boolean screenChanged) {
        super.onBoundsChanged(screenChanged);
        if (screenChanged)
            textX = iconX = iconY = UNDEFINED;
    }

    @Override
    public void onPaint(Graphics g) {
        g.foreColor = this.foreColor;

        Font bkpFont = this.font;
        Font iconFont = Font.getFont(iconType.fontName(), true, 18);

        if (textX == UNDEFINED) {
            int totalTextAndIconWidth = iconFont.fm.stringWidth(iconType.toString())
                    + UnitsConverter.toPixels(DP + 8) + bkpFont.fm.stringWidth(originalText);
            int desiredIconX = (getWidth() - totalTextAndIconWidth) / 2;
            setText(originalText);
            textX = (getWidth() - fm.stringWidth(text)) / 2;

            if (iconPosition == IconPosition.LEFT) {
                iconX = textX
                        - UnitsConverter.toPixels(DP + 8)
                        - iconFont.fm.stringWidth(iconType.toString());
            } else {
                iconX = textX
                        + UnitsConverter.toPixels(DP + 8)
                        + fm.stringWidth(text);
            }
            iconY = (getHeight() - iconFont.fm.height) / 2;
            int spaceSize = fm.stringWidth(" ");
            while (iconX < desiredIconX && textX > 0) { //shift until icon and text get on the middle
                setText(" " + getText());
                iconX += spaceSize;
            }

        }

        super.onPaint(g);

        g.setFont(iconFont);
        g.foreColor = foreColor;
        g.drawText(iconType.toString(), iconX, iconY);
        g.setFont(bkpFont); // restore font
    }
}
