package com.barley.ast;

import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

public class ConstantAST implements AST {

    private BarleyValue constant;

    public ConstantAST(BarleyValue constant) {
        this.constant = constant;
    }

    @Override
    public BarleyValue execute() {
        return constant;
    }
}
