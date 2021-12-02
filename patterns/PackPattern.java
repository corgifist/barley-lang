package com.barley.patterns;

public class PackPattern extends Pattern {

    private String name;

    public PackPattern(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
