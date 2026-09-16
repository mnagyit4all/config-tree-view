package main.ui.providers;

import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;

import org.eclipse.jface.viewers.ITreeContentProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigTreeContentProvider implements ITreeContentProvider {

    private ConfigGraph currentGraph;

    @Override
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof ConfigGraph) {
            this.currentGraph = (ConfigGraph) inputElement;

            // 1. Elsődleges: Explicit beállított gyökér elem
            if (currentGraph.getRootNode() != null && currentGraph.getRootNode().isVisible()) {
                return new Object[] { currentGraph.getRootNode() };
            }

            // 2. Tartalék (Fallback): Megkeressük azokat a látható csomópontokat, amelyeket senki sem importál
            Set<ConfigNode> targets = new HashSet<>();
            for (ConfigEdge edge : currentGraph.getEdges()) {
                targets.add(edge.getTarget());
            }

            List<ConfigNode> roots = new ArrayList<>();
            for (ConfigNode node : currentGraph.getNodes()) {
                if (!targets.contains(node) && node.isVisible()) {
                    roots.add(node);
                }
            }

            if (!roots.isEmpty()) {
                return roots.toArray();
            }

            // 3. Ha minden elem körkörös függőségben van, az első látható csomópontot adjuk vissza
            for (ConfigNode node : currentGraph.getNodes()) {
                if (node.isVisible()) {
                    return new Object[] { node };
                }
            }
        }
        return new Object[0];
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof ConfigNode && currentGraph != null) {
            ConfigNode parentNode = (ConfigNode) parentElement;
            List<ConfigNode> children = new ArrayList<>();

            for (ConfigEdge edge : currentGraph.getEdges()) {
                if (edge.getSource().equals(parentNode) && edge.getTarget().isVisible()) {
                    children.add(edge.getTarget());
                }
            }
            return children.toArray();
        }
        return new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return getChildren(element).length > 0;
    }
}