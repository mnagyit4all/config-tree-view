package main.ui.views;

import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.part.ViewPart;

import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Single Instance ViewPart a Spring konfigurációs hálózat Zest gráfos megjelenítésére.
 */
public class ConfigGraphViewPart extends ViewPart {

    public static final String ID = "ui.views.ConfigGraphViewPart";

    private Graph graphWidget;
    private final Map<ConfigNode, GraphNode> nodeMap = new HashMap<>();

    // Színek inicializálása
    private Color greenColor;
    private Color redColor;
    private Color whiteColor;

    @Override
    public void createPartControl(Composite parent) {
        graphWidget = new Graph(parent, SWT.NONE);
        graphWidget.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);

        Display display = parent.getDisplay();
        greenColor = new Color(display, 144, 238, 144); // Light Green
        redColor = new Color(display, 255, 102, 102);   // Light Red
        whiteColor = new Color(display, 255, 255, 255);

        graphWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                java.util.List<?> selectedItems = graphWidget.getSelection();
                if (!selectedItems.isEmpty() && selectedItems.get(0) instanceof GraphNode) {
                    GraphNode selectedNode = (GraphNode) selectedItems.get(0);
                    ConfigNode configNode = (ConfigNode) selectedNode.getData();
                    openInEditor(configNode);
                }
            }
        });
    }

    /**
     * Megjelenített gráf felülírása az új eredményekkel.
     */
    public void updateGraph(ConfigGraph configGraph) {
        // Korábbi elemek törlése
        clearGraph();

        if (configGraph == null || configGraph.getNodes().isEmpty()) {
            return;
        }

        // Csomópontok kirajzolása
        for (ConfigNode node : configGraph.getNodes()) {
            GraphNode gNode = new GraphNode(graphWidget, SWT.NONE, node.getDisplayName());
            gNode.setData(node);

            // Cirkuláris függőség alapján színezés (Piros = körben lévő, Zöld = érvényes)
            if (node.isCyclic()) {
                gNode.setBackgroundColor(redColor);
            } else {
                gNode.setBackgroundColor(greenColor);
            }
            gNode.setForegroundColor(whiteColor);

            nodeMap.put(node, gNode);
        }

        // Irányított élek/nyilak meghúzása
        for (ConfigEdge edge : configGraph.getEdges()) {
            GraphNode sourceGNode = nodeMap.get(edge.getSource());
            GraphNode targetGNode = nodeMap.get(edge.getTarget());

            if (sourceGNode != null && targetGNode != null) {
                new GraphConnection(graphWidget, ZestStyles.CONNECTIONS_DIRECTED, sourceGNode, targetGNode);
            }
        }

        // Gráf elrendezésének frissítése
        graphWidget.applyLayout();
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

    @Override
    public void setFocus() {
        if (graphWidget != null && !graphWidget.isDisposed()) {
            graphWidget.setFocus();
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