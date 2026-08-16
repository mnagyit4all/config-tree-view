package main.core;

import main.model.ConfigGraph;
import main.model.ConfigNode;
import main.validation.CycleDetector;
import main.ui.dialogs.DiscoveryModeDialog.DiscoveryMode;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphBuilderService {

    private final WorkspaceConfigSearcher searcher = new WorkspaceConfigSearcher();

    public ConfigGraph buildGraph(ICompilationUnit rootUnit, DiscoveryMode mode) {
        ConfigGraph graph = new ConfigGraph();
        Map<String, ICompilationUnit> workspaceConfigs = searcher.buildConfigMap();

        if (rootUnit == null) {
            return graph;
        }

        // 1. Gyökér csomópont feldolgozása
        String rootFqn = getFullyQualifiedName(rootUnit);
        ConfigNode rootNode = new ConfigNode(rootFqn, rootUnit);

        SpringConfigAstVisitor rootVisitor = parseAst(rootUnit);
        if (!rootVisitor.isConfiguration()) {
            // Ha a kiválasztott fájl nem rendelkezik @Configuration annotációval, üres gráfot adunk vissza
            return graph;
        }

        graph.addNode(rootNode);

        // 2. Lefelé történő rekurzív feltárás (Show below)
        Queue<ICompilationUnit> queue = new ArrayDeque<>();
        Set<String> processedFqns = new HashSet<>();

        queue.add(rootUnit);
        processedFqns.add(rootFqn);

        while (!queue.isEmpty()) {
            ICompilationUnit currentUnit = queue.poll();
            String currentFqn = getFullyQualifiedName(currentUnit);
            ConfigNode currentNode = graph.findNodeByFqn(currentFqn)
                    .orElseGet(() -> new ConfigNode(currentFqn, currentUnit));

            SpringConfigAstVisitor visitor = parseAst(currentUnit);

            for (String importedName : visitor.getImportedClassNames()) {
                ICompilationUnit targetUnit = workspaceConfigs.get(importedName);

                if (targetUnit != null) {
                    SpringConfigAstVisitor targetVisitor = parseAst(targetUnit);

                    // Kizárólag a @Configuration annotációval rendelkező elemeket adjuk hozzá
                    if (targetVisitor.isConfiguration()) {
                        String targetFqn = getFullyQualifiedName(targetUnit);
                        ConfigNode targetNode = graph.findNodeByFqn(targetFqn)
                                .orElseGet(() -> new ConfigNode(targetFqn, targetUnit));

                        graph.addEdge(currentNode, targetNode);

                        if (!processedFqns.contains(targetFqn)) {
                            processedFqns.add(targetFqn);
                            queue.add(targetUnit);
                        }
                    }
                }
            }
        }

        // 3. Teljes bejárás (Show in workspace - le és fel)
        if (mode == DiscoveryMode.SHOW_IN_WORKSPACE) {
            List<ICompilationUnit> allWorkspaceUnits = searcher.findAllWorkspaceConfigs();

            for (ICompilationUnit unit : allWorkspaceUnits) {
                String unitFqn = getFullyQualifiedName(unit);

                SpringConfigAstVisitor visitor = parseAst(unit);
                if (visitor.isConfiguration()) {
                    for (String importedName : visitor.getImportedClassNames()) {
                        ICompilationUnit targetUnit = workspaceConfigs.get(importedName);

                        if (targetUnit != null) {
                            String targetFqn = getFullyQualifiedName(targetUnit);

                            // Ha az importált elem már a meglévő hálózat része
                            if (graph.findNodeByFqn(targetFqn).isPresent()) {
                                ConfigNode sourceNode = graph.findNodeByFqn(unitFqn)
                                        .orElseGet(() -> new ConfigNode(unitFqn, unit));
                                ConfigNode targetNode = graph.findNodeByFqn(targetFqn).get();

                                graph.addEdge(sourceNode, targetNode);
                            }
                        }
                    }
                }
            }
        }

        // 4. Cirkuláris függőség ellenőrzés futtatása
        CycleDetector.detectCycles(graph);

        return graph;
    }

    private SpringConfigAstVisitor parseAst(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(unit);
        parser.setResolveBindings(true);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        SpringConfigAstVisitor visitor = new SpringConfigAstVisitor();
        cu.accept(visitor);
        return visitor;
    }

    private String getFullyQualifiedName(ICompilationUnit unit) {
        String fileName = unit.getElementName();
        String className = fileName.substring(0, fileName.lastIndexOf(".java"));
        String packageName = unit.getParent().getElementName();
        return packageName.isEmpty() ? className : packageName + "." + className;
    }
}