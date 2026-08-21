package main.validation;

import main.model.BeanModel;
import main.model.ConfigEdge;
import main.model.ConfigGraph;
import main.model.ConfigNode;

import java.util.*;

public class BeanValidator {

    public static void validateBeans(ConfigGraph graph) {
        if (graph == null || graph.getRootNode() == null) {
            return;
        }

        for (ConfigNode node : graph.getNodes()) {
            node.setHasInvalidBean(false);
            for (BeanModel bean : node.getBeans()) {
                bean.setValid(true);
            }
        }
        for (ConfigEdge edge : graph.getEdges()) {
            edge.setInvalid(false);
        }

        ConfigNode root = graph.getRootNode();
        Queue<ConfigNode> queue = new ArrayDeque<>();
        Map<ConfigNode, Set<String>> parentBeansMap = new HashMap<>();

        queue.add(root);
        parentBeansMap.put(root, new HashSet<>());

        Set<ConfigNode> directlyInvalidNodes = new HashSet<>();
        Set<String> activeFilters = BeanFilterManager.getInstance().getActiveFilters();

        while (!queue.isEmpty()) {
            ConfigNode current = queue.poll();
            Set<String> inheritedBeans = parentBeansMap.getOrDefault(current, Collections.emptySet());
            Set<String> currentEffectiveBeans = new HashSet<>(inheritedBeans);

            for (BeanModel bean : current.getBeans()) {
                boolean isFiltered = isBeanFiltered(bean, activeFilters);

                if (!isFiltered && inheritedBeans.contains(bean.getName())) {
                    bean.setValid(false);
                    current.setHasInvalidBean(true);
                    directlyInvalidNodes.add(current);
                }
                
                currentEffectiveBeans.add(bean.getName());
            }

            for (ConfigEdge edge : graph.getEdges()) {
                if (edge.getSource().equals(current)) {
                    ConfigNode target = edge.getTarget();

                    if (!parentBeansMap.containsKey(target)) {
                        parentBeansMap.put(target, new HashSet<>(currentEffectiveBeans));
                        queue.add(target);
                    } else {
                        parentBeansMap.get(target).addAll(currentEffectiveBeans);
                    }
                }
            }
        }

        for (ConfigNode invalidNode : directlyInvalidNodes) {
            propagateInvalidStatusUpward(graph, invalidNode, new HashSet<>());
        }
    }

    private static boolean isBeanFiltered(BeanModel bean, Set<String> activeFilters) {
        if (activeFilters.isEmpty() || bean.getAnnotations().isEmpty()) {
            return false;
        }
        for (String activeFilter : activeFilters) {
            if (bean.hasAnnotation(activeFilter)) {
                return true;
            }
        }
        return false;
    }

    private static void propagateInvalidStatusUpward(ConfigGraph graph, ConfigNode currentNode, Set<ConfigNode> visited) {
        if (visited.contains(currentNode)) {
            return;
        }
        visited.add(currentNode);
        currentNode.setHasInvalidBean(true);

        for (ConfigEdge edge : graph.getEdges()) {
            if (edge.getTarget().equals(currentNode)) {
                edge.setInvalid(true);
                propagateInvalidStatusUpward(graph, edge.getSource(), visited);
            }
        }
    }
}