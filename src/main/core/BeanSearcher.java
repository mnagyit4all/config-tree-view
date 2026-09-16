package main.core;

import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Szűri a ConfigGraph csomópontjait a megadott bean név alapján.
 * Megtartja a hierarchikus fa struktúrát: a szülő látható marad, ha benne vagy bármelyik leszármazottjában szerepel a bean.
 */
public class BeanSearcher {

    /**
     * Szűri a teljes gráfot a megadott bean név alapján.
     * 
     * @param graph a szűrendő gráf
     * @param beanName a keresett bean neve (ha null vagy üres, minden csomópont isVisible = true lesz)
     */
    public void filterByBean(ConfigGraph graph, String beanName) {
        if (graph == null || graph.isEmpty()) {
            return;
        }

        // Üres keresés esetén minden node-ot láthatóvá teszünk
        if (beanName == null || beanName.trim().isEmpty()) {
            for (ConfigNode node : graph.getNodes()) {
                node.setVisible(true);
            }
            return;
        }

        String searchTarget = beanName.trim();
        Set<ConfigNode> visited = new HashSet<>();

        // Gyökér elemek meghatározása (explicit root, vagy in-degree 0 elemek)
        List<ConfigNode> rootNodes = getRootNodes(graph);

        // Rekurzív szűrés a gyökerektől indítva
        for (ConfigNode root : rootNodes) {
            evaluateVisibility(root, graph, searchTarget, visited);
        }

        // Árva / elszigetelt csomópontok ellenőrzése, amiket a gyökerekből nem értünk el
        for (ConfigNode node : graph.getNodes()) {
            if (!visited.contains(node)) {
                evaluateVisibility(node, graph, searchTarget, visited);
            }
        }
    }

    private boolean evaluateVisibility(ConfigNode node, ConfigGraph graph, String beanName, Set<ConfigNode> visited) {
        if (node == null) return false;

        // Körkörös függőség elleni védőháló
        if (visited.contains(node)) {
            return node.isVisible();
        }
        visited.add(node);

        // Direct match: Tartalmazza-e az adott node közvetlenül a beant?
        boolean directMatch = node.getBeans().stream()
                .anyMatch(b -> b.getName().toLowerCase().contains(beanName.toLowerCase()));

        // Child match: Tartalmazza-e valamelyik gyermek/leszármazott a beant?
        boolean childMatch = false;
        List<ConfigNode> children = getChildren(node, graph);

        for (ConfigNode child : children) {
            boolean isChildVisible = evaluateVisibility(child, graph, beanName, visited);
            if (isChildVisible) {
                childMatch = true;
            }
        }

        // A node látható marad, ha saját maga match-el VAGY bármelyik gyermeke match-el
        boolean isVisible = directMatch || childMatch;
        node.setVisible(isVisible);

        return isVisible;
    }

    private List<ConfigNode> getChildren(ConfigNode parent, ConfigGraph graph) {
        List<ConfigNode> children = new ArrayList<>();
        for (ConfigEdge edge : graph.getEdges()) {
            if (edge.getSource().equals(parent)) {
                children.add(edge.getTarget());
            }
        }
        return children;
    }

    private List<ConfigNode> getRootNodes(ConfigGraph graph) {
        List<ConfigNode> roots = new ArrayList<>();
        if (graph.getRootNode() != null) {
            roots.add(graph.getRootNode());
            return roots;
        }

        Set<ConfigNode> targets = new HashSet<>();
        for (ConfigEdge edge : graph.getEdges()) {
            targets.add(edge.getTarget());
        }

        for (ConfigNode node : graph.getNodes()) {
            if (!targets.contains(node)) {
                roots.add(node);
            }
        }
        return roots;
    }
}