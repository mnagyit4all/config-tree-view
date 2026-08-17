package main.ui.views.components;

import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.ui.providers.ConfigTreeContentProvider;
import main.ui.providers.ConfigTreeLabelProvider;
import main.ui.views.helpers.EditorNavigator;
import main.ui.views.helpers.ViewColorManager;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;

import java.util.function.BooleanSupplier;

public class StructuredTreeComposite extends Composite {

    private final TreeViewer treeViewer;

    public StructuredTreeComposite(Composite parent, ViewColorManager colorManager, BooleanSupplier showDetailsSupplier, IWorkbenchPage page) {
        super(parent, SWT.NONE);
        this.setLayout(new org.eclipse.swt.layout.FillLayout()); 

        treeViewer = new TreeViewer(this, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer.setContentProvider(new ConfigTreeContentProvider());
        treeViewer.setLabelProvider(new ConfigTreeLabelProvider(colorManager, showDetailsSupplier));

        treeViewer.addDoubleClickListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selection.getFirstElement() instanceof ConfigNode) {
                EditorNavigator.openInEditor(page, (ConfigNode) selection.getFirstElement());
            }
        });
    }
    public void addSelectionListener(java.util.function.Consumer<ConfigNode> onNodeSelected) {
        treeViewer.addSelectionChangedListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selection.getFirstElement() instanceof ConfigNode) {
                onNodeSelected.accept((ConfigNode) selection.getFirstElement());
            }
        });
    }

    public void setInput(ConfigGraph configGraph) {
        treeViewer.setInput(configGraph);
    }

    public void refresh() {
        treeViewer.refresh();
    }
}