package main.ui.views.components;

import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.ui.views.helpers.EditorNavigator;
import main.ui.views.helpers.ViewColorManager;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphConnection;
import org.eclipse.zest.core.widgets.GraphNode;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZestGraphComposite extends Composite {

    private final Graph graphWidget;
    private final Map<ConfigNode, GraphNode> nodeMap = new HashMap<>();

    public ZestGraphComposite(Composite parent, ViewColorManager colorManager, IWorkbenchPage page) {
        super(parent, SWT.NONE);
        this.setLayout(new org.eclipse.swt.layout.FillLayout());
        
        graphWidget = new Graph(this, SWT.NONE);
        graphWidget.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
        
        graphWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                List<?> selectedItems = graphWidget.getSelection();
                if (!selectedItems.isEmpty() && selectedItems.get(0) instanceof GraphNode) {
                    GraphNode selectedNode = (GraphNode) selectedItems.get(0);
                    EditorNavigator.openInEditor(page, (ConfigNode) selectedNode.getData());
                }
            }
        });
    }

    public void updateGraph(ConfigGraph configGraph, ViewColorManager colorManager) {
        clearGraph();
        if (configGraph == null || configGraph.getNodes().isEmpty()) return;

        for (ConfigNode node : configGraph.getNodes()) {
            GraphNode gNode = new GraphNode(graphWidget, SWT.NONE, node.getDisplayName());
            gNode.setData(node);
            gNode.setBackgroundColor(node.isCyclic() ? colorManager.getRedColor() : colorManager.getGreenColor());
            gNode.setForegroundColor(colorManager.getWhiteColor());
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
}