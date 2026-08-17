package main.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private boolean hasInvalidBean = false;
    private final List<BeanModel> beans = new ArrayList<>();

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

    public List<BeanModel> getBeans() {
        return Collections.unmodifiableList(beans);
    }

    public void addBean(BeanModel bean) {
        if (bean != null && !beans.contains(bean)) {
            beans.add(bean);
        }
    }

    public boolean hasInvalidBean() {
        return hasInvalidBean;
    }

    public void setHasInvalidBean(boolean hasInvalidBean) {
        this.hasInvalidBean = hasInvalidBean;
    }

    public boolean isCyclic() {
        return cyclic;
    }

    public void setCyclic(boolean cyclic) {
        this.cyclic = cyclic;
    }

    /**
     * Szöveges státusz a Structured View felirataihoz (2.4 pont alapján).
     */
    public String getStatusTag() {
        if (cyclic) {
            return "[CIRCULAR]";
        }
        if (hasInvalidBean) {
            return "[INVALID_BEAN]";
        }
        return "[OK]";
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
        return displayName + " " + getStatusTag();
    }
}