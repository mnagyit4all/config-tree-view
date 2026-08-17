package main.model;

import java.util.Objects;

/**
 * Egy Spring @Configuration osztályban deklarált @Bean-t képvisel.
 */
public class BeanModel {

    private final String name;
    private boolean valid = true; // Alapértelmezetten érvényes, duplikáció esetén false

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