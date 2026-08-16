package main.ui.views;

import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.ui.providers.ConfigTreeContentProvider;
import main.ui.providers.ConfigTreeLabelProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.part.ViewPart;

import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigGraphViewPart extends ViewPart {

    public static final String ID = "ui.views.ConfigGraphViewPart";

    private Composite container;
    private StackLayout stackLayout;
    
    private Graph graphWidget;
    private TreeViewer treeViewer;

    private final Map<ConfigNode, GraphNode> nodeMap = new HashMap<>();
    private boolean showDetails = false;

    private Color greenColor;
    private Color redColor;
    private Color whiteColor;
    
    public Color getGreenColor() {
        return greenColor;
    }

    public Color getRedColor() {
        return redColor;
    }

    @Override
    public void createPartControl(Composite parent) {
        container = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        container.setLayout(stackLayout);

        Display display = parent.getDisplay();
        greenColor = new Color(display, 144, 238, 144);
        redColor = new Color(display, 255, 102, 102);
        whiteColor = new Color(display, 255, 255, 255);

        // 1. Graph Widget
        graphWidget = new Graph(container, SWT.NONE);
        graphWidget.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        graphWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                List<?> selectedItems = graphWidget.getSelection();
                if (!selectedItems.isEmpty() && selectedItems.get(0) instanceof GraphNode) {
                    GraphNode selectedNode = (GraphNode) selectedItems.get(0);
                    openInEditor((ConfigNode) selectedNode.getData());
                }
            }
        });

        // 2. Tree Viewer (Structured view)
        treeViewer = new TreeViewer(container, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer.setContentProvider(new ConfigTreeContentProvider());
        treeViewer.setLabelProvider(new ConfigTreeLabelProvider(this));

        treeViewer.addDoubleClickListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            if (selection.getFirstElement() instanceof ConfigNode) {
                openInEditor((ConfigNode) selection.getFirstElement());
            }
        });

        // Alapértelmezett: Gráf nézet
        stackLayout.topControl = graphWidget;
        container.layout();

        createViewMenu();
    }

    private void createViewMenu() {
        IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();

        Action graphViewAction = new Action("Graph view", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    stackLayout.topControl = graphWidget;
                    container.layout(true, true);
                }
            }
        };
        graphViewAction.setChecked(true);

        Action structuredViewAction = new Action("Structured view", IAction.AS_RADIO_BUTTON) {
            @Override
            public void run() {
                if (isChecked()) {
                    stackLayout.topControl = treeViewer.getControl();
                    container.layout(true, true);
                }
            }
        };

        Action showDetailsAction = new Action("Show details", IAction.AS_CHECK_BOX) {
            @Override
            public void run() {
                showDetails = isChecked();
                treeViewer.refresh();
            }
        };
        showDetailsAction.setChecked(false);

        menuManager.add(graphViewAction);
        menuManager.add(structuredViewAction);
        menuManager.add(new Separator());
        menuManager.add(showDetailsAction);
    }

    public void updateGraph(ConfigGraph configGraph) {
        clearGraph();

        if (configGraph == null || configGraph.getNodes().isEmpty()) {
            treeViewer.setInput(null);
            return;
        }

        // 1. Zest Gráf frissítése
        for (ConfigNode node : configGraph.getNodes()) {
            GraphNode gNode = new GraphNode(graphWidget, SWT.NONE, node.getDisplayName());
            gNode.setData(node);
            gNode.setBackgroundColor(node.isCyclic() ? redColor : greenColor);
            gNode.setForegroundColor(whiteColor);
            nodeMap.put(node, gNode);
        }

        for (ConfigEdge edge : configGraph.getEdges()) {
            GraphNode sourceGNode = nodeMap.get(edge.getSource());
            GraphNode targetGNode = nodeMap.get(edge.getTarget());

            if (sourceGNode != null && targetGNode != null) {
                new GraphConnection(graphWidget, ZestStyles.CONNECTIONS_DIRECTED, sourceGNode, targetGNode);
            }
        }
        graphWidget.applyLayout();

        // 2. TreeViewer frissítése (beküldjük az egész gráfot, a provider kiszűri a rootNode-ot)
        treeViewer.setInput(configGraph);
    }

    private void clearGraph() {
        for (Object item : graphWidget.getConnections().toArray()) {
            ((GraphConnection) item).dispose();
        }
        for (Object item : graphWidget.getNodes().toArray()) {
            ((GraphNode) item).dispose();
        }
        nodeMap.clear();
    }

    private void openInEditor(ConfigNode configNode) {
        if (configNode == null) return;
        try {
            if (configNode.getCompilationUnit() != null) {
                JavaUI.openInEditor(configNode.getCompilationUnit());
            } else if (configNode.getFile() != null) {
                IFile file = configNode.getFile();
                org.eclipse.ui.ide.IDE.openEditor(getSite().getPage(), file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    @Override
    public void setFocus() {
        if (container != null && !container.isDisposed()) {
            container.setFocus();
        }
    }

    @Override
    public void dispose() {
        if (greenColor != null && !greenColor.isDisposed()) greenColor.dispose();
        if (redColor != null && !redColor.isDisposed()) redColor.dispose();
        if (whiteColor != null && !whiteColor.isDisposed()) whiteColor.dispose();
        super.dispose();
    }
}