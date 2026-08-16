package main.ui.providers;

import main.model.ConfigNode;
import main.ui.views.ConfigGraphViewPart;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;

public class ConfigTreeLabelProvider extends LabelProvider implements IColorProvider {

    private final ConfigGraphViewPart viewPart;

    public ConfigTreeLabelProvider(ConfigGraphViewPart viewPart) {
        this.viewPart = viewPart;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ConfigNode) {
            ConfigNode node = (ConfigNode) element;
            String label = node.getDisplayName();

            if (viewPart.isShowDetails()) {
                label += node.isCyclic() ? " [CIRCULAR]" : " [OK]";
            }
            return label;
        }
        return super.getText(element);
    }

    @Override
    public Color getForeground(Object element) {
        if (element instanceof ConfigNode) {
            ConfigNode node = (ConfigNode) element;
            // Cirkuláris függőség esetén piros, különben zöld betűszín
            return node.isCyclic() ? viewPart.getRedColor() : viewPart.getGreenColor();
        }
        return null; // Alapértelmezett SWT szövegszín
    }

    @Override
    public Color getBackground(Object element) {
        return null; // Alapértelmezett hátteret használ
    }
}