package main.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import main.model.ConfigGraph;

public class BeanFilterManager {

    private static BeanFilterManager INSTANCE;

    // Elérhető szűrő annotációk
    private final Set<String> availableFilters = new LinkedHashSet<>();
    // Aktívan bepipált szűrők
    private final Set<String> activeFilters = new HashSet<>();

    private BeanFilterManager(ConfigGraph configGraph) {
        if (configGraph != null && configGraph.getNodes() != null) {
            configGraph.getNodes().forEach(n -> {
                if (n.getBeans() != null) {
                    n.getBeans().forEach(b -> {
                        if (b.getAnnotations() != null) {
                            b.getAnnotations().forEach(a -> {
                            	if (a != null && a.startsWith("@")) {
                            	    addAvailableFilter(a);
                            	}
                            });
                        }
                    });
                }
            });
        }
    }
    
    public static synchronized BeanFilterManager initialize(ConfigGraph configGraph) {
        if (INSTANCE == null) {
        	INSTANCE = new BeanFilterManager(configGraph);
        }
        return INSTANCE;
    }
    
    public static void clearFilter() {
    	INSTANCE = null;
    }

    public static BeanFilterManager getInstance() {
        return INSTANCE;
    }

    public void addAvailableFilter(String filter) {
        if (filter != null && !filter.trim().isEmpty()) {
            availableFilters.add(filter.trim());
        }
    }

    public Set<String> getAvailableFilters() {
        return Collections.unmodifiableSet(availableFilters);
    }

    public Set<String> getActiveFilters() {
        return Collections.unmodifiableSet(activeFilters);
    }

    public boolean isFilterActive(String filter) {
        return activeFilters.contains(filter);
    }

    public void setActiveFilters(Set<String> newActiveFilters) {
        activeFilters.clear();
        if (newActiveFilters != null) {
            activeFilters.addAll(newActiveFilters);
        }
    }
}