package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyClosure;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.UserFunction;
import com.barley.utils.AST;

import java.io.Serializable;

public class ClosureAST implements AST, Serializable {

    private UserFunction function;

    public ClosureAST(UserFunction function) {
        this.function = function;
    }

    @Override
    public BarleyValue execute() {
        return new BarleyClosure(function);
    }

    @Override
    public void visit(Optimization optimization) {
    }

    @Override
    public String toString() {
        return function.toString();
    }
}
