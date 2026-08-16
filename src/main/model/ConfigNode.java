package main.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;

import java.util.Objects;

/**
 * Egy Spring @Configuration osztályt képviselő csomópont a gráfban.
 */
public class ConfigNode {

    private final String fullyQualifiedName;
    private final String displayName;
    private final ICompilationUnit compilationUnit;
    private final IFile file;
    private boolean cyclic = false;

    public ConfigNode(String fullyQualifiedName, ICompilationUnit compilationUnit) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.compilationUnit = compilationUnit;
        this.file = compilationUnit != null ? (IFile) compilationUnit.getResource() : null;
        this.displayName = extractSimpleName(fullyQualifiedName);
    }

    public ConfigNode(String fullyQualifiedName, IFile file) {
        this.fullyQualifiedName = fullyQualifiedName;
        this.compilationUnit = null;
        this.file = file;
        this.displayName = extractSimpleName(fullyQualifiedName);
    }

    private String extractSimpleName(String fqn) {
        if (fqn == null || fqn.isEmpty()) return "";
        int lastDot = fqn.lastIndexOf('.');
        return lastDot != -1 ? fqn.substring(lastDot + 1) : fqn;
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ICompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    public IFile getFile() {
        return file;
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigNode that = (ConfigNode) o;
        return Objects.equals(fullyQualifiedName, that.fullyQualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullyQualifiedName);
    }

    @Override
    public String toString() {
        return displayName + (cyclic ? " [CYCLIC]" : "");
    }
}