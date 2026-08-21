package main.ui.dialogs;

import main.validation.BeanFilterManager;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import java.util.HashSet;
import java.util.Set;

public class BeanFilterDialog extends TitleAreaDialog {

    private CheckboxTableViewer checkboxViewer;
    private Text searchText;
    private final BeanFilterManager filterManager;
    private String filterText = "";

    public BeanFilterDialog(Shell parentShell) {
        super(parentShell);
        this.filterManager = BeanFilterManager.getInstance();
    }

    @Override
    public void create() {
        super.create();
        setTitle("Bean Validation Filters");
        setMessage("Select annotation filters to bypass bean duplication rules.");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Kereső mező felirata és beviteli mezője
        Label searchLabel = new Label(container, SWT.NONE);
        searchLabel.setText("Search filters:");

        searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
        searchText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        searchText.setMessage("Type to filter...");
        searchText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                filterText = searchText.getText().trim().toLowerCase();
                checkboxViewer.refresh();
            }
        });

        Label listLabel = new Label(container, SWT.NONE);
        listLabel.setText("Available Filters:");

        checkboxViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData tableData = new GridData(GridData.FILL_BOTH);
        tableData.heightHint = 180;
        checkboxViewer.getTable().setLayoutData(tableData);

        checkboxViewer.setContentProvider(ArrayContentProvider.getInstance());
        checkboxViewer.setLabelProvider(new LabelProvider());

        checkboxViewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                if (filterText.isEmpty()) {
                    return true;
                }
                return element.toString().toLowerCase().contains(filterText);
            }
        });

        checkboxViewer.setInput(filterManager.getAvailableFilters());

        for (String filter : filterManager.getAvailableFilters()) {
            if (filterManager.isFilterActive(filter)) {
                checkboxViewer.setChecked(filter, true);
            }
        }

        return area;
    }

    @Override
    protected void okPressed() {
        Set<String> selectedFilters = new HashSet<>();
        for (Object checkedElement : checkboxViewer.getCheckedElements()) {
            selectedFilters.add((String) checkedElement);
        }
        filterManager.setActiveFilters(selectedFilters);

        super.okPressed();
    }

    @Override
    protected boolean isResizable() {
        return true;
    }
}