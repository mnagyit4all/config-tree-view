package main.core;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkspaceConfigSearcher {

    /**
     * Összes Workspace-ben található *Config.java forrásfájl feltárása.
     */
    public List<ICompilationUnit> findAllWorkspaceConfigs() {
        List<ICompilationUnit> configUnits = new ArrayList<>();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IJavaProject[] projects;

        try {
            projects = JavaCore.create(root).getJavaProjects();
            for (IJavaProject project : projects) {
                if (!project.isOpen()) continue;

                for (IPackageFragmentRoot pkgRoot : project.getPackageFragmentRoots()) {
                    // Kizárólag a Workspace forrásmappáit vizsgáljuk (külső JAR-okat nem)
                    if (pkgRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        for (IJavaElement child : pkgRoot.getChildren()) {
                            if (child instanceof IPackageFragment) {
                                IPackageFragment pkg = (IPackageFragment) child;
                                for (ICompilationUnit unit : pkg.getCompilationUnits()) {
                                    if (unit.getElementName().endsWith("Config.java")) {
                                        configUnits.add(unit);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CoreException e) {
            // Hibás/nem fordítható elemek esetén kihagyás a stabil működésért
            e.printStackTrace();
        }

        return configUnits;
    }

    /**
     * Felépít egy gyorstárat (Map) a Workspace-ben található konfigurációkról FQN és Simple Name alapján.
     */
    public Map<String, ICompilationUnit> buildConfigMap() {
        Map<String, ICompilationUnit> map = new HashMap<>();
        List<ICompilationUnit> units = findAllWorkspaceConfigs();

        for (ICompilationUnit unit : units) {
            String fileName = unit.getElementName();
            String className = fileName.substring(0, fileName.lastIndexOf(".java"));
            
            // Csomagnév lekérése
            String packageName = unit.getParent().getElementName();
            String fqn = packageName.isEmpty() ? className : packageName + "." + className;

            map.put(fqn, unit);
            map.put(className, unit); // Fallback kereséshez egyszerű név alapján
        }

        return map;
    }
}