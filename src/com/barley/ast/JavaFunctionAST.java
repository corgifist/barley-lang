package com.barley.ast;

import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;
import com.barley.utils.Function;

import java.util.Arrays;

public class JavaFunctionAST implements AST {

    private Function function;
    private BarleyValue[] args;

    public JavaFunctionAST(Function function, BarleyValue[] args) {
        this.function = function;
        this.args = args;
    }

    @Override
    public BarleyValue execute() {
        return function.execute(args);
    }

    @Override
    public String toString() {
        return function.toString() + Arrays.asList(args);
    }
}
