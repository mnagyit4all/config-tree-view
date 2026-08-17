package main.ui.views.helpers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ViewColorManager {
    private Color greenColor;
    private Color redColor;
    private Color whiteColor;

    public ViewColorManager(Display display) {
        this.greenColor = new Color(display, 144, 238, 144);
        this.redColor = new Color(display, 255, 102, 102);
        this.whiteColor = new Color(display, 255, 255, 255);
    }

    public Color getGreenColor() { return greenColor; }
    public Color getRedColor() { return redColor; }
    public Color getWhiteColor() { return whiteColor; }

    public void dispose() {
        if (greenColor != null && !greenColor.isDisposed()) greenColor.dispose();
        if (redColor != null && !redColor.isDisposed()) redColor.dispose();
        if (whiteColor != null && !whiteColor.isDisposed()) whiteColor.dispose();
    }
}