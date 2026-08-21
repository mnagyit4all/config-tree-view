package main.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BeanFilterManager {

    private static final BeanFilterManager INSTANCE = new BeanFilterManager();

    // Elérhető szűrő annotációk
    private final Set<String> availableFilters = new LinkedHashSet<>();
    // Aktívan bepipált szűrők
    private final Set<String> activeFilters = new HashSet<>();

    private BeanFilterManager() {
        // Alapértelmezett FS szerinti szűrő hozzáadása
        addAvailableFilter("@ConditionalOnMissingBean");
        addAvailableFilter("@ConditionalOnMissingClass");
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