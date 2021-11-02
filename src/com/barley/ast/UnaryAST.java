package com.barley.ast;

import com.barley.runtime.*;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.lang.reflect.AnnotatedType;

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
            case 'n': return not(val1);
            default:
               badArith();
        }
        return null;
    }

    private BarleyValue not(BarleyValue value) {
        if (value instanceof BarleyNumber) {
            return new BarleyAtom(AtomTable.put(String.valueOf(value.asInteger().intValue() != 0)));
        } else if (value instanceof BarleyString) {
            return new BarleyAtom(AtomTable.put(String.valueOf(value.toString().isEmpty())));
        } else if (value instanceof BarleyList) {
            return new BarleyAtom(AtomTable.put(String.valueOf(value.toString().equals("[]"))));
        } else badArith();
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
