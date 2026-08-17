package main.ui.providers;

import main.model.ConfigNode;
import main.ui.views.helpers.ViewColorManager;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;

import java.util.function.BooleanSupplier;

public class ConfigTreeLabelProvider extends LabelProvider implements IColorProvider {

    private final ViewColorManager colorManager;
    private final BooleanSupplier showDetailsSupplier;

    public ConfigTreeLabelProvider(ViewColorManager colorManager, BooleanSupplier showDetailsSupplier) {
        this.colorManager = colorManager;
        this.showDetailsSupplier = showDetailsSupplier;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ConfigNode) {
            ConfigNode node = (ConfigNode) element;
            String label = node.getDisplayName();
            if (showDetailsSupplier.getAsBoolean()) {
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
            return node.isCyclic() ? colorManager.getRedColor() : colorManager.getGreenColor();
        }
        return null;
    }

    @Override
    public Color getBackground(Object element) {
        return null;
    }
}