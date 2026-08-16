package main.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Párbeszédablak a függőségi fa feltárási módjának kiválasztásához.
 */
public class DiscoveryModeDialog extends TitleAreaDialog {

    public enum DiscoveryMode {
        SHOW_BELOW,
        SHOW_IN_WORKSPACE
    }

    private Button btnShowBelow;
    private Button btnShowInWorkspace;
    private DiscoveryMode selectedMode = DiscoveryMode.SHOW_BELOW;

    public DiscoveryModeDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Spring Configuration Discovery Mode");
        setMessage("Válassza ki a config tree feltárásának terjedelmét!");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 15;
        layout.marginHeight = 15;
        layout.verticalSpacing = 10;
        container.setLayout(layout);

        btnShowBelow = new Button(container, SWT.RADIO);
        btnShowBelow.setText("Show below (Lefelé feltárás)");
        btnShowBelow.setSelection(true);
        btnShowBelow.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        btnShowInWorkspace = new Button(container, SWT.RADIO);
        btnShowInWorkspace.setText("Show in workspace (Teljes bejárás - le és fel)");
        btnShowInWorkspace.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        return area;
    }

    @Override
    protected void okPressed() {
        if (btnShowInWorkspace.getSelection()) {
            selectedMode = DiscoveryMode.SHOW_IN_WORKSPACE;
        } else {
            selectedMode = DiscoveryMode.SHOW_BELOW;
        }
        super.okPressed();
    }

    public DiscoveryMode getSelectedMode() {
        return selectedMode;
    }
}