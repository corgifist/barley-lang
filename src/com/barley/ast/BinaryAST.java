package com.barley.ast;

import com.barley.runtime.*;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedList;

public class BinaryAST implements AST, Serializable  {

    public final AST expr1, expr2;
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
        if (val1 instanceof BarleyList) {
            BarleyList list1 = (BarleyList) val1;
            switch (op) {
                case '+':
                    if (!(val2 instanceof BarleyList))
                        badArith(val1, val2);
                    BarleyList list2 = (BarleyList) val2;
                    LinkedList<BarleyValue> result = new LinkedList<>();
                    result.addAll(list1.getList());
                    result.addAll(list2.getList());
                    return new BarleyList(result);
                case '=':
                    return new BarleyAtom(addAtom(String.valueOf(list1.equals(val1))));
                default: badArith(val1, val2);
            }
        }

        if (val1 instanceof BarleyString || val2 instanceof BarleyString) {
            String str1 = val1.toString();
            String str2 = val2.toString();
            switch (op) {
                case '+': return new BarleyString(str1 + str2);
                case '=': return new BarleyAtom(addAtom(String.valueOf(str1.equals(str2))));
                default: badArith(val1, val2);
            }
        }

        BigDecimal decimal1 = val1.asFloat();
        BigDecimal decimal2 = val2.asFloat();
        double number1 = val1.asFloat().doubleValue();
        double number2 = val2.asFloat().doubleValue();

        switch (op) {
            case '-': return new BarleyNumber(decimal1.subtract(decimal2));
            case '*': return new BarleyNumber(decimal1.multiply(decimal2));
            case '/': return new BarleyNumber(decimal1.divide(decimal2));
            case '+': return new BarleyNumber(decimal1.add(decimal2));
            case '>': return new BarleyAtom(addAtom(String.valueOf(number1 > number2)));
            case '<': return new BarleyAtom(addAtom(String.valueOf(number1 < number2)));
            case 't': return new BarleyAtom(addAtom(String.valueOf(number1 <= number2)));
            case 'g': return new BarleyAtom(addAtom(String.valueOf(number1 >= number2)));
            case '=': return new BarleyAtom(addAtom(String.valueOf(number1 == number2)));
            case 'a': return new BarleyNumber(addAtom(String.valueOf(istrue(val1) && istrue(val2))));
            case 'o': return new BarleyNumber(addAtom(String.valueOf(istrue(val1) || istrue(val2))));
            default:
               badArith(val1, val2);
        }
        return null;
    }

    private boolean istrue(BarleyValue value) {
        return value.toString().equals("true");
    }

    public void badArith(BarleyValue val1, BarleyValue val2) {
        throw new BarleyException("BadArithmetic", "an error occurred when evaluation an arithmetic expression\n  called as: \n    "  + String.format("%s %s %s", val1, op, val2));
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", expr1, op, expr2);
    }

    private int addAtom(String atom) {
        return AtomTable.put(atom);
    }

}
