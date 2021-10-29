package com.barley.patterns;

public class VariablePattern extends Pattern {

    private String variable;

    public VariablePattern(String variable) {
        this.variable = variable;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    public String toString() {
        return variable;
    }
}
