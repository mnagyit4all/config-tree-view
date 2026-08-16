package main.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A Spring konfigurációs függőségi hálózatot összefogó gráf modell.
 */
public class ConfigGraph {

    private ConfigNode rootNode; // ÚJ: A kijelölt kezdő konfiguráció (gyökér)
    private final Set<ConfigNode> nodes = new HashSet<>();
    private final Set<ConfigEdge> edges = new HashSet<>();

    public ConfigNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(ConfigNode rootNode) {
        this.rootNode = rootNode;
        if (rootNode != null) {
            addNode(rootNode);
        }
    }

    public void addNode(ConfigNode node) {
        if (node != null) {
            nodes.add(node);
        }
    }

    public void addEdge(ConfigNode source, ConfigNode target) {
        if (source != null && target != null) {
            addNode(source);
            addNode(target);
            edges.add(new ConfigEdge(source, target));
        }
    }

    public Optional<ConfigNode> findNodeByFqn(String fullyQualifiedName) {
        return nodes.stream()
                .filter(n -> n.getFullyQualifiedName().equals(fullyQualifiedName))
                .findFirst();
    }

    public Set<ConfigNode> getNodes() {
        return Collections.unmodifiableSet(nodes);
    }

    public Set<ConfigEdge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    public void clear() {
        rootNode = null;
        nodes.clear();
        edges.clear();
    }

    public boolean isEmpty() {
        return nodes.isEmpty();
    }
}