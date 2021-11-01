package com.barley.ast;

import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;

public class ConstantAST implements AST, Serializable {

    private BarleyValue constant;

    public ConstantAST(BarleyValue constant) {
        this.constant = constant;
    }

    @Override
    public BarleyValue execute() {
        return constant;
    }

    @Override
    public String toString() {
        return constant.toString();
    }
}
