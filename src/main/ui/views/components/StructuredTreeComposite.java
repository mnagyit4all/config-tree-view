package main.ui.views.components;

import main.core.BeanSearcher;
import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.ui.providers.ConfigTreeContentProvider;
import main.ui.providers.ConfigTreeLabelProvider;
import main.ui.views.helpers.EditorNavigator;
import main.ui.views.helpers.ViewColorManager;

import java.util.function.Supplier;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;

public class StructuredTreeComposite extends Composite {

    private final TreeViewer treeViewer;
    private final Text searchField;
    private final BeanSearcher beanSearcher = new BeanSearcher();
    private ConfigGraph currentGraph;

    public StructuredTreeComposite(Composite parent, ViewColorManager colorManager, Supplier<Boolean> showDetailsSupplier, IWorkbenchPage page) {
        super(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);

        // Keresőmező létrehozása
        searchField = new Text(this, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        searchField.setMessage("Search bean...");
        GridData searchData = new GridData(GridData.FILL_HORIZONTAL);
        searchField.setLayoutData(searchData);

        // Alapértelmezetten elrejtjük a keresősávot
        setSearchBarVisible(false);

        // Élő szűrés gépeléskor (ModifyListener)
        searchField.addModifyListener(e -> performSearch());

        // TreeViewer inicializálása
        treeViewer = new TreeViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        treeViewer.setContentProvider(new ConfigTreeContentProvider());
        treeViewer.setLabelProvider(new ConfigTreeLabelProvider(colorManager, showDetailsSupplier::get));
        treeViewer.addDoubleClickListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selection.getFirstElement() instanceof ConfigNode) {
                EditorNavigator.openInEditor(page, (ConfigNode) selection.getFirstElement());
            }
        });
    }

    public void setInput(ConfigGraph graph) {
        this.currentGraph = graph;
        performSearch();
    }

    private void performSearch() {
        if (currentGraph != null) {
            String query = searchField.getText();
            beanSearcher.filterByBean(currentGraph, query);
            treeViewer.setInput(currentGraph);
            treeViewer.refresh();
        }
        if(!searchField.getText().isEmpty()) {        	
        	treeViewer.expandAll();        	
        }
    }

    public void setSearchBarVisible(boolean visible) {
        GridData data = (GridData) searchField.getLayoutData();
        data.exclude = !visible;
        searchField.setVisible(visible);
        if (!visible) {
            searchField.setText(""); // Ha elrejtjük, töröljük a keresőt
        }
        this.layout(true, true);
    }

    public void addSelectionListener(java.util.function.Consumer<ConfigNode> onNodeSelected) {
        treeViewer.addSelectionChangedListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (!selection.isEmpty() && selection.getFirstElement() instanceof ConfigNode) {
                onNodeSelected.accept((ConfigNode) selection.getFirstElement());
            } else {
                onNodeSelected.accept(null);
            }
        });
    }

    public void refresh() {
        treeViewer.refresh();
    }
}