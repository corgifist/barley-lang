package com.barley.ast;

import com.barley.runtime.*;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

public class BinaryAST implements AST{

    private final AST expr1, expr2;
    private char op;

    public BinaryAST(AST expr1, AST expr2, char op) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.op = op;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue val1 = expr1.execute();
        BarleyValue val2 = expr2.execute();

        if (val1 instanceof BarleyString || val2 instanceof BarleyString) {
            String str1 = val1.toString();
            String str2 = val2.toString();
            switch (op) {
                case '+': return new BarleyString(str1 + str2);
                default: badArith();
            }
        }

        double number1 = val1.asFloat().doubleValue();
        double number2 = val2.asFloat().doubleValue();

        switch (op) {
            case '-': return new BarleyNumber(number1 - number2);
            case '*': return new BarleyNumber(number1 * number2);
            case '/': return new BarleyNumber(number1 / number2);
            case '+': return new BarleyNumber(number1 + number2);
            case '>': return new BarleyAtom(addAtom(String.valueOf(number1 > number2)));
            case '<': return new BarleyAtom(addAtom(String.valueOf(number1 < number2)));
            case 't': return new BarleyAtom(addAtom(String.valueOf(number1 <= number2)));
            case 'g': return new BarleyAtom(addAtom(String.valueOf(number1 >= number2)));
            case '=': return new BarleyAtom(addAtom(String.valueOf(number1 == number2)));
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
        return String.format("%s %s %s", expr1, op, expr2);
    }

    private int addAtom(String atom) {
        return AtomTable.put(atom);
    }

}
