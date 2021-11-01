package com.barley.patterns;

import com.barley.runtime.BarleyValue;

import java.io.Serializable;

public class ConstantPattern extends Pattern implements Serializable {

    private BarleyValue constant;

    public ConstantPattern(BarleyValue constant) {
        this.constant = constant;
    }

    public BarleyValue getConstant() {
        return constant;
    }

    @Override
    public String toString() {
        return constant.toString();
    }
}
