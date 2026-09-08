package main.ui.views;

import main.model.BeanModel;
import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.ui.dialogs.BeanFilterDialog;
import main.ui.views.components.StructuredTreeComposite;
import main.ui.views.components.ZestGraphComposite;
import main.ui.views.helpers.ViewColorManager;
import main.validation.BeanValidator;

import java.net.URI;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class SpringConfigViewPart extends ViewPart {

    public static final String ID = "ui.views.SpringConfigViewPart";

    private SashForm mainSashForm;
    private Composite topContainer;
    private StackLayout stackLayout;
    
    private ZestGraphComposite graphComposite;
    private StructuredTreeComposite treeComposite;
    private TableViewer beanTableViewer;
    private ViewColorManager colorManager;

    private boolean showBeanDetails = false;
    private boolean showDetails = false;

    private ConfigGraph currentGraph;
    private Shell optionsShell;

    @Override
    public void createPartControl(Composite parent) {
        colorManager = new ViewColorManager(parent.getDisplay());

        mainSashForm = new SashForm(parent, SWT.VERTICAL);

        // Felső panel (Gráf / Struktúra nézet váltó)
        topContainer = new Composite(mainSashForm, SWT.NONE);
        stackLayout = new StackLayout();
        topContainer.setLayout(stackLayout);

        graphComposite = new ZestGraphComposite(topContainer, colorManager, getSite().getPage());
        treeComposite = new StructuredTreeComposite(topContainer, colorManager, () -> showDetails, getSite().getPage());

        stackLayout.topControl = graphComposite;

        // Alsó panel (Bean részletező panel)
        beanTableViewer = new TableViewer(mainSashForm, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        beanTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        beanTableViewer.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                if (element instanceof BeanModel) {
                    return ((BeanModel) element).getName();
                }
                return super.getText(element);
            }

            @Override
            public Color getForeground(Object element) {
                if (element instanceof BeanModel) {
                    BeanModel bean = (BeanModel) element;
                    return bean.isValid() ? colorManager.getGreenColor() : colorManager.getRedColor();
                }
                return null;
            }
        });

        // Node kijelölések bekötése az alsó panel frissítésére
        graphComposite.addSelectionListener(this::displayBeansForNode);
        treeComposite.addSelectionListener(this::displayBeansForNode);

        // Alapértelmezetten elrejtjük az alsó panelt (100% / 0%)
        mainSashForm.setWeights(new int[]{100, 0});

        createToolBarMenu();
    }

    private void displayBeansForNode(ConfigNode node) {
        if (node != null) {
            beanTableViewer.setInput(node.getBeans());
        } else {
            beanTableViewer.setInput(null);
        }
    }

    public void updateGraph(ConfigGraph configGraph) {
        this.currentGraph = configGraph; // Eltároljuk az aktuális gráf referenciáját
        graphComposite.updateGraph(configGraph, colorManager);
        treeComposite.setInput(configGraph);
        beanTableViewer.setInput(null);
    }

    private void createToolBarMenu() {
        IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();

        Action openOptionsAction = new Action("",Action.AS_PUSH_BUTTON) {
            @Override
            public void runWithEvent(Event event) {
                if (event.widget instanceof ToolItem) {
                    ToolItem item = (ToolItem) event.widget;
                    showOptionsPopover(item);
                }
            }
        };
        try {
            URI iconUri = new URI("platform:/plugin/org.eclipse.ui.navigator/icons/full/clcl16/elipses.svg");
            openOptionsAction.setImageDescriptor(ImageDescriptor.createFromURL(iconUri.toURL()));
        } catch (Exception e) {
            openOptionsAction.setText("...");
        }
        toolbarManager.add(openOptionsAction);
    }

    private void showOptionsPopover(ToolItem toolItem) {
        // Ha már nyitva van, bezárjuk
        if (optionsShell != null && !optionsShell.isDisposed()) {
            optionsShell.dispose();
            return;
        }

        // Keret nélküli, mindig felül lévő Popup Shell létrehozása
        optionsShell = new Shell(getSite().getShell(), SWT.ON_TOP | SWT.TOOL | SWT.BORDER);
        optionsShell.setLayout(new GridLayout(1, false));

        // --- View Mode Radio Group ---
        Button graphViewRadio = new Button(optionsShell, SWT.RADIO);
        graphViewRadio.setText("Graph view");
        graphViewRadio.setSelection(stackLayout.topControl == graphComposite);
        graphViewRadio.addListener(SWT.Selection, e -> {
            if (graphViewRadio.getSelection()) {
                stackLayout.topControl = graphComposite;
                topContainer.layout(true, true);
            }
        });

        Button structuredViewRadio = new Button(optionsShell, SWT.RADIO);
        structuredViewRadio.setText("Structured view");
        structuredViewRadio.setSelection(stackLayout.topControl == treeComposite);
        structuredViewRadio.addListener(SWT.Selection, e -> {
            if (structuredViewRadio.getSelection()) {
                stackLayout.topControl = treeComposite;
                topContainer.layout(true, true);
            }
        });

        // Elválasztó vonal
        Label separator1 = new Label(optionsShell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // --- Checkboxok ---
        Button showDetailsCheck = new Button(optionsShell, SWT.CHECK);
        showDetailsCheck.setText("Show details");
        showDetailsCheck.setSelection(showDetails);
        showDetailsCheck.addListener(SWT.Selection, e -> {
            showDetails = showDetailsCheck.getSelection();
            treeComposite.refresh();
        });

        Button showBeanDetailsCheck = new Button(optionsShell, SWT.CHECK);
        showBeanDetailsCheck.setText("Show bean details");
        showBeanDetailsCheck.setSelection(showBeanDetails);
        showBeanDetailsCheck.addListener(SWT.Selection, e -> {
            showBeanDetails = showBeanDetailsCheck.getSelection();
            if (showBeanDetails) {
                mainSashForm.setWeights(new int[]{70, 30});
            } else {
                mainSashForm.setWeights(new int[]{100, 0});
            }
        });

        // Elválasztó vonal
        Label separator2 = new Label(optionsShell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // --- Filter Gomb ---
        Button addFilterButton = new Button(optionsShell, SWT.PUSH);
        addFilterButton.setText("Add filter...");
        addFilterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addFilterButton.addListener(SWT.Selection, e -> {
            BeanFilterDialog dialog = new BeanFilterDialog(getSite().getShell());
            if (dialog.open() == IDialogConstants.OK_ID) {
                if (currentGraph != null) {
                    BeanValidator.validateBeans(currentGraph);
                    treeComposite.refresh();
                    graphComposite.updateGraph(currentGraph, colorManager);
                    beanTableViewer.refresh();
                }
            }
        });
        
        // Pozicionálás és képernyő-kilógás elleni védelem
        Point shellSize = optionsShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        Rectangle rect = toolItem.getBounds();
        Point pt = toolItem.getParent().toDisplay(rect.x, rect.y);

        // Alapból a gomb jobb sarka alá igazítjuk (a popup jobb széle a gomb jobb széléhez illeszkedik)
        int x = (pt.x + rect.width) - shellSize.x;
        int y = pt.y + rect.height;

        // Képernyő szélének ellenőrzése és leszorítása (nem tud kilógni)
        Rectangle screen = optionsShell.getDisplay().getBounds();
        x = Math.max(0, Math.min(x, screen.width - shellSize.x));
        y = Math.max(0, Math.min(y, screen.height - shellSize.y));

        optionsShell.setBounds(x, y, shellSize.x, shellSize.y);

        // Automatikus bezárás, ha a felhasználó mellékattint (elveszíti a fókuszt)
        Listener deactivateListener = event -> {
            optionsShell.getDisplay().asyncExec(() -> {
                if (optionsShell != null && !optionsShell.isDisposed()) {
                    optionsShell.dispose();
                }
            });
        };
        optionsShell.addListener(SWT.Deactivate, deactivateListener);

        optionsShell.open();
    }

    @Override
    public void setFocus() {
        if (topContainer != null && !topContainer.isDisposed()) {
            topContainer.setFocus();
        }
    }

    @Override
    public void dispose() {
        if (optionsShell != null && !optionsShell.isDisposed()) {
            optionsShell.dispose();
        }
        if (colorManager != null) {
            colorManager.dispose();
        }
        super.dispose();
    }
}