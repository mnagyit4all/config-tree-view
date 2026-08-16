package main.model;

import java.util.Objects;

/**
 * Irányított él két konfigurációs csomópont között.
 * A forrás (source) osztály az @Import annotáció segítségével importálja a cél (target) osztályt.
 */
public class ConfigEdge {

    private final ConfigNode source;
    private final ConfigNode target;

    public ConfigEdge(ConfigNode source, ConfigNode target) {
        this.source = source;
        this.target = target;
    }

    public ConfigNode getSource() {
        return source;
    }

    public ConfigNode getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigEdge edge = (ConfigEdge) o;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return source.getDisplayName() + " -> " + target.getDisplayName();
    }
}