package main.ui.views;

import main.model.ConfigGraph;
import main.ui.views.components.StructuredTreeComposite;
import main.ui.views.components.ZestGraphComposite;
import main.ui.views.helpers.ViewColorManager;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SpringConfigViewPart extends ViewPart {

    public static final String ID = "ui.views.SpringConfigViewPart";

    private Composite container;
    private StackLayout stackLayout;
    
    private ZestGraphComposite graphComposite;
    private StructuredTreeComposite treeComposite;
    private ViewColorManager colorManager;

    private boolean showDetails = false;
    
    @Override
    public void createPartControl(Composite parent) {
        colorManager = new ViewColorManager(parent.getDisplay());

        container = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        container.setLayout(stackLayout);

        // Alkomponensek példányosítása
        graphComposite = new ZestGraphComposite(container, colorManager, getSite().getPage());
        treeComposite = new StructuredTreeComposite(container, colorManager, () -> showDetails, getSite().getPage());

        stackLayout.topControl = graphComposite;
        container.layout();

        createViewMenu();
    }

    public void updateGraph(ConfigGraph configGraph) {
        graphComposite.updateGraph(configGraph, colorManager);
        treeComposite.setInput(configGraph);
    }

    private void createViewMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        Action graphViewAction = new Action("Graph view", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    stackLayout.topControl = graphComposite;
                    container.layout(true, true);
                }
            }
        };
        graphViewAction.setChecked(true);

        Action structuredViewAction = new Action("Structured view", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    stackLayout.topControl = treeComposite;
                    container.layout(true, true);
                }
            }
        };

        Action showDetailsAction = new Action("Show details", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                showDetails = isChecked();
                treeComposite.refresh();
            }
        };

        menuManager.add(graphViewAction);
        menuManager.add(structuredViewAction);
        menuManager.add(new Separator());
        menuManager.add(showDetailsAction);
    }

    @Override
    public void setFocus() {
        if (container != null && !container.isDisposed()) {
            container.setFocus();
        }
    }

    @Override
    public void dispose() {
        if (colorManager != null) {
            colorManager.dispose();
        }
        super.dispose();
    }
}