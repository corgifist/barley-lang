package com.barley.optimizations;

import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

public final class VariableInfo {
    public AST value;
    int modifications;

    public VariableInfo(AST value, int modifications) {
        this.value = value;
        this.modifications = modifications;
    }

    @Override
    public String toString() {
        return (value == null ? "?" : value) + " (" + modifications + " mods)";
    }
}
