package main.validation;

import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory kördetektáló algoritmus (Tarjan SCC).
 * Kizárólag azokat a csomópontokat jelöli meg cirkulárisként (isCyclic = true),
 * amelyek közvetlenül részesei legalább egy zárt függőségi körnek.
 */
public class CycleDetector {

    public static void detectCycles(ConfigGraph graph) {
        if (graph == null || graph.getNodes().isEmpty()) {
            return;
        }

        // Szomszédsági lista (Adjacency List) felépítése
        Map<ConfigNode, List<ConfigNode>> adj = new HashMap<>();
        for (ConfigNode node : graph.getNodes()) {
            adj.put(node, new ArrayList<>());
            node.setCyclic(false); // Alaphelyzetbe állítás
        }
        for (ConfigEdge edge : graph.getEdges()) {
            if (adj.containsKey(edge.getSource())) {
                adj.get(edge.getSource()).add(edge.getTarget());
            }
        }

        // Tarjan-algoritmus változói
        Map<ConfigNode, Integer> indexMap = new HashMap<>();
        Map<ConfigNode, Integer> lowLinkMap = new HashMap<>();
        Set<ConfigNode> onStack = new HashSet<>();
        Deque<ConfigNode> stack = new ArrayDeque<>();
        int[] indexCounter = new int[]{0};

        for (ConfigNode node : graph.getNodes()) {
            if (!indexMap.containsKey(node)) {
                strongConnect(node, adj, indexMap, lowLinkMap, onStack, stack, indexCounter);
            }
        }
    }

    private static void strongConnect(
            ConfigNode u,
            Map<ConfigNode, List<ConfigNode>> adj,
            Map<ConfigNode, Integer> indexMap,
            Map<ConfigNode, Integer> lowLinkMap,
            Set<ConfigNode> onStack,
            Deque<ConfigNode> stack,
            int[] indexCounter) {

        indexMap.put(u, indexCounter[0]);
        lowLinkMap.put(u, indexCounter[0]);
        indexCounter[0]++;
        stack.push(u);
        onStack.add(u);

        List<ConfigNode> neighbors = adj.getOrDefault(u, Collections.emptyList());
        for (ConfigNode v : neighbors) {
            if (!indexMap.containsKey(v)) {
                strongConnect(v, adj, indexMap, lowLinkMap, onStack, stack, indexCounter);
                lowLinkMap.put(u, Math.min(lowLinkMap.get(u), lowLinkMap.get(v)));
            } else if (onStack.contains(v)) {
                lowLinkMap.put(u, Math.min(lowLinkMap.get(u), indexMap.get(v)));
            }
        }

        // Ha 'u' egy Erősen Összefüggő Komponens (SCC) gyökere
        if (lowLinkMap.get(u).equals(indexMap.get(u))) {
            List<ConfigNode> scc = new ArrayList<>();
            ConfigNode v;
            do {
                v = stack.pop();
                onStack.remove(v);
                scc.add(v);
            } while (!v.equals(u));

            // Ha az SCC több mint 1 elemet tartalmaz, az összes elem zárt körben van
            if (scc.size() > 1) {
                for (ConfigNode node : scc) {
                    node.setCyclic(true);
                }
            } else if (scc.size() == 1) {
                // Ha 1 elemű az SCC, csak akkor cirkuláris, ha önmagára mutat (Self-loop)
                ConfigNode singleNode = scc.get(0);
                if (adj.getOrDefault(singleNode, Collections.emptyList()).contains(singleNode)) {
                    singleNode.setCyclic(true);
                }
            }
        }
    }
}