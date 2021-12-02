package com.barley.ast;

import com.barley.Main;
import com.barley.optimizations.Optimization;
import com.barley.runtime.*;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.LinkedList;

public class BinaryAST implements AST, Serializable {

    public AST expr1;
    public AST expr2;
    public char op;
    private String current;
    private int line;

    public BinaryAST(AST expr1, AST expr2, char op, int line, String current) {
        this.expr1 = expr1;
        this.expr2 = expr2;
        this.op = op;
        this.current = current;
        this.line = line;
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
                default:
                    badArith(val1, val2);
            }
        }

        if (val1 instanceof BarleyString || val2 instanceof BarleyString) {
            String str1 = val1.toString();
            String str2 = val2.toString();
            switch (op) {
                case '+':
                    return new BarleyString(str1 + str2);
                case '=':
                    return new BarleyAtom(addAtom(String.valueOf(str1.equals(str2))));
                default:
                    badArith(val1, val2);
            }
        }

        switch (op) {
            case '-':
                return new BarleyNumber(val1.asFloat().subtract(val2.asFloat()));
            case '*':
                return new BarleyNumber(val1.asFloat().multiply(val2.asFloat()));
            case '/':
                return new BarleyNumber(val1.asFloat().divide(val2.asFloat(), new MathContext(2, RoundingMode.HALF_UP)));
            case '+':
                return new BarleyNumber(val1.asFloat().add(val2.asFloat()));
            case '>':
                return new BarleyAtom(addAtom(String.valueOf(val1.asFloat().doubleValue() > val2.asFloat().doubleValue())));
            case '<':
                return new BarleyAtom(addAtom(String.valueOf(val1.asFloat().doubleValue() < val2.asFloat().doubleValue())));
            case 't':
                return new BarleyAtom(addAtom(String.valueOf(val1.asFloat().doubleValue() <= val2.asFloat().doubleValue())));
            case 'g':
                return new BarleyAtom(addAtom(String.valueOf(val1.asFloat().doubleValue() >= val2.asFloat().doubleValue())));
            case '=':
                return new BarleyAtom(addAtom(String.valueOf(val1.equals(val2))));
            case 'a':
                return new BarleyAtom(addAtom(String.valueOf(istrue(val1) && istrue(val2))));
            case 'o':
                return new BarleyAtom(addAtom(String.valueOf(istrue(val1) || istrue(val2))));
            default:
                badArith(val1, val2);
        }
        return null;
    }

    @Override
    public void visit(Optimization optimization) {
        this.expr1 = optimization.optimize(expr1);
        this.expr2 = optimization.optimize(expr2);
    }

    private boolean istrue(BarleyValue value) {
        return value.toString().equals("true");
    }

    public void badArith(BarleyValue val1, BarleyValue val2) {
        Main.error("BadArith", "an error has occurred when evaluating an arithmetic expression", line, current);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", expr1, op, expr2);
    }

    private int addAtom(String atom) {
        return AtomTable.put(atom);
    }
}
