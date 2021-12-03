package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyPointer;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;

public class PointerAST implements AST, Serializable {

    private AST value;

    public PointerAST(AST value) {
        this.value = value;
    }

    @Override
    public BarleyValue execute() {
        return new BarleyPointer(value.execute());
    }

    @Override
    public void visit(Optimization optimization) {
        value.visit(optimization);
    }

    @Override
    public String toString() {
        return "#" + value;
    }
}
