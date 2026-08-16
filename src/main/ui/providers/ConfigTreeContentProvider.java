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
            if (currentGraph.getRootNode() != null) {
                return new Object[] { currentGraph.getRootNode() };
            }

            // 2. Tartalék (Fallback): Megkeressük azokat a csomópontokat, amelyeket senki sem importál (in-degree = 0)
            Set<ConfigNode> targets = new HashSet<>();
            for (ConfigEdge edge : currentGraph.getEdges()) {
                targets.add(edge.getTarget());
            }

            List<ConfigNode> roots = new ArrayList<>();
            for (ConfigNode node : currentGraph.getNodes()) {
                if (!targets.contains(node)) {
                    roots.add(node);
                }
            }

            if (!roots.isEmpty()) {
                return roots.toArray();
            }

            // 3. Ha minden elem körkörös függőségben van, az első csomópontot adjuk vissza
            if (!currentGraph.getNodes().isEmpty()) {
                return new Object[] { currentGraph.getNodes().iterator().next() };
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
                if (edge.getSource().equals(parentNode)) {
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