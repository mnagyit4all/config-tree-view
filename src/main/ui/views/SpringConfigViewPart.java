package main.ui.views;

import main.model.BeanModel;
import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.ui.dialogs.BeanFilterDialog;
import main.ui.views.components.StructuredTreeComposite;
import main.ui.views.components.ZestGraphComposite;
import main.ui.views.helpers.ViewColorManager;
import main.validation.BeanValidator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.viewers.ColumnLabelProvider;

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

        createViewMenu();
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

    private void createViewMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        Action graphViewAction = new Action("Graph view", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    stackLayout.topControl = graphComposite;
                    topContainer.layout(true, true);
                }
            }
        };
        graphViewAction.setChecked(true);

        Action structuredViewAction = new Action("Structured view", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    stackLayout.topControl = treeComposite;
                    topContainer.layout(true, true);
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

        Action showBeanDetailsAction = new Action("Show bean details", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                showBeanDetails = isChecked();
                if (showBeanDetails) {
                    mainSashForm.setWeights(new int[]{70, 30});
                } else {
                    mainSashForm.setWeights(new int[]{100, 0});
                }
            }
        };

        Action addFilterAction = new Action("Add filter...") {
            @Override
            public void run() {
                BeanFilterDialog dialog = new BeanFilterDialog(getSite().getShell());
                if (dialog.open() == IDialogConstants.OK_ID) {
                    if (currentGraph != null) {
                        // Újravalidálás az új aktív szűrőkkel
                        BeanValidator.validateBeans(currentGraph);
                        
                        // Nézetek frissítése (4. HIBA JAVÍTVA: graphComposite.updateGraph hívása refresh() helyett)
                        treeComposite.refresh();
                        graphComposite.updateGraph(currentGraph, colorManager);
                        beanTableViewer.refresh();
                    }
                }
            }
        };

        menuManager.add(graphViewAction);
        menuManager.add(structuredViewAction);
        menuManager.add(new Separator());
        menuManager.add(showDetailsAction);
        menuManager.add(showBeanDetailsAction);
        menuManager.add(new Separator());
        menuManager.add(addFilterAction);
    }

    @Override
    public void setFocus() {
        if (topContainer != null && !topContainer.isDisposed()) {
            topContainer.setFocus();
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