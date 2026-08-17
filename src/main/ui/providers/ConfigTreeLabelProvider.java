package main.ui.providers;

import main.model.ConfigNode;
import main.ui.views.helpers.ViewColorManager;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Color;

import java.util.function.BooleanSupplier;

public class ConfigTreeLabelProvider extends ColumnLabelProvider {

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
            if (showDetailsSupplier != null && showDetailsSupplier.getAsBoolean()) {
                return node.getDisplayName() + " " + node.getStatusTag();
            }
            return node.getDisplayName();
        }
        return super.getText(element);
    }

    @Override
    public Color getForeground(Object element) {
        if (element instanceof ConfigNode) {
            ConfigNode node = (ConfigNode) element;
            if (node.isCyclic() || node.hasInvalidBean()) {
                return colorManager.getRedColor();
            }
            return colorManager.getGreenColor();
        }
        return null;
    }
}