package main.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Egy Spring @Configuration osztályban deklarált @Bean-t képvisel.
 */
public class BeanModel {

    private final String name;
    private boolean valid = true; // Alapértelmezetten érvényes, duplikáció esetén false
    private final Set<String> annotations = new HashSet<>(); 

    public BeanModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Set<String> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(String annotation) {
        if (annotation != null && !annotation.trim().isEmpty()) {
            this.annotations.add(annotation.trim());
        }
    }

    public boolean hasAnnotation(String annotation) {
        return annotations.contains(annotation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeanModel beanModel = (BeanModel) o;
        return Objects.equals(name, beanModel.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + (valid ? " [OK]" : " [DUPLICATE]");
    }
}