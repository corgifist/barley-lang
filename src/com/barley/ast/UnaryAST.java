package com.barley.ast;

import com.barley.runtime.BarleyNumber;
import com.barley.runtime.BarleyString;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.io.Serializable;

public class UnaryAST implements AST, Serializable {

    private final AST expr1;
    private char op;

    public UnaryAST(AST expr1, char op) {
        this.expr1 = expr1;
        this.op = op;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue val1 = expr1.execute();

        double number1 = val1.asFloat().doubleValue();

        switch (op) {
            case '-': return new BarleyNumber(-number1);
            default:
               badArith();
        }
        return null;
    }

    public void badArith() {
        throw new BarleyException("BadArithmetic", "an error occurred when evaluation an arithmetic expression\n  called as: \n    "  + this);
    }

    @Override
    public String toString() {
        return String.format("%s%s", expr1, op);
    }
}
