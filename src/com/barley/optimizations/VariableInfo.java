package com.barley.optimizations;

import com.barley.runtime.BarleyValue;

public final class VariableInfo {
    public BarleyValue value;
    public int modifications;

    public VariableInfo(BarleyValue value, int modifications) {
        this.value = value;
        this.modifications = modifications;
    }

    @Override
    public String toString() {
        return (value == null ? "?" : value) + " (" + modifications + " mods)";
    }
}
