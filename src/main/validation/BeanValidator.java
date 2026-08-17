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

        ConfigNode root = graph.getRootNode();
        Queue<ConfigNode> queue = new ArrayDeque<>();
        Map<ConfigNode, Set<String>> parentBeansMap = new HashMap<>();

        queue.add(root);
        parentBeansMap.put(root, new HashSet<>());

        Set<ConfigNode> directlyInvalidNodes = new HashSet<>();

        while (!queue.isEmpty()) {
            ConfigNode current = queue.poll();
            Set<String> inheritedBeans = parentBeansMap.getOrDefault(current, Collections.emptySet());
            Set<String> currentEffectiveBeans = new HashSet<>(inheritedBeans);

            for (BeanModel bean : current.getBeans()) {
                if (inheritedBeans.contains(bean.getName())) {
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