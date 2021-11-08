package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyNumber;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class CaseAST implements AST, Serializable {

    public final AST expression;
    public final List<Pattern> patterns;

    public CaseAST(AST expression, List<Pattern> patterns) {
        this.expression = expression;
        this.patterns = patterns;
    }

    @Override
    public BarleyValue execute() {
        return eval();
    }

    @Override
    public void visit(Optimization optimization) {

    }

    public BarleyValue eval() {
        final BarleyValue value = expression.execute();
        for (Pattern p : patterns) {
            if (p instanceof ConstantPattern) {
                final ConstantPattern pattern = (ConstantPattern) p;
                if (match(value, pattern.constant) && optMatches(p)) {
                    return evalResult(p.result);
                }
            }
            if (p instanceof VariablePattern) {
                final VariablePattern pattern = (VariablePattern) p;
                if (pattern.variable.equals("_")) return evalResult(p.result);

                if (Table.isExists(pattern.variable)) {
                    if (match(value, Table.get(pattern.variable)) && optMatches(p)) {
                        return evalResult(p.result);
                    }
                } else {
                    Table.define(pattern.variable, value);
                    if (optMatches(p)) {
                        final BarleyValue result = evalResult(p.result);
                        Table.remove(pattern.variable);
                        return result;
                    }
                    Table.remove(pattern.variable);
                }
            }
            if ((value instanceof BarleyList) && (p instanceof ListPattern)) {
                final ListPattern pattern = (ListPattern) p;
                if (matchListPattern((BarleyList) value, pattern)) {
                    // Clean up variables if matched
                    final BarleyValue result = evalResult(p.result);
                    for (String var : pattern.parts) {
                        Table.remove(var);
                    }
                    return result;
                }
            }
            if ((value instanceof BarleyList) && (p instanceof TuplePattern)) {
                final TuplePattern pattern = (TuplePattern) p;
                if (matchTuplePattern((BarleyList) value, pattern) && optMatches(p)) {
                    return evalResult(p.result);
                }
            }
        }
        throw new BarleyException("BadMatch", "no patterns were matched. patterns: " + patterns);
    }

    private boolean matchTuplePattern(BarleyList array, TuplePattern p) {
        if (p.values.size() != array.getList().size()) return false;

        final int size = array.getList().size();
        for (int i = 0; i < size; i++) {
            final AST expr = p.values.get(i);
            if ((expr != TuplePattern.ANY) && (expr.execute().equals(expr))) {
                return false;
            }
        }
        return true;
    }

    private boolean matchListPattern(BarleyList array, ListPattern p) {
        final List<String> parts = p.parts;
        final int partsSize = parts.size();
        final int arraySize = array.getList().size();
        switch (partsSize) {
            case 0: // match [] { case []: ... }
                if ((arraySize == 0) && optMatches(p)) {
                    return true;
                }
                return false;

            case 1: // match arr { case [x]: x = arr ... }
                final String variable = parts.get(0);
                Table.define(variable, array);
                if (optMatches(p)) {
                    return true;
                }
                Table.remove(variable);
                return false;

            default: { // match arr { case [...]: .. }
                if (partsSize == arraySize) {
                    // match [0, 1, 2] { case [a::b::c]: a=0, b=1, c=2 ... }
                    return matchListPatternEqualsSize(p, parts, partsSize, array);
                } else if (partsSize < arraySize) {
                    // match [1, 2, 3] { case [head :: tail]: ... }
                    return matchListPatternWithTail(p, parts, partsSize, array, arraySize);
                }
                return false;
            }
        }
    }

    private boolean matchListPatternEqualsSize(ListPattern p, List<String> parts, int partsSize, BarleyList array) {
        // Set variables
        for (int i = 0; i < partsSize; i++) {
            Table.define(parts.get(i), array.getList().get(i));
        }
        if (optMatches(p)) {
            // Clean up will be provided after evaluate result
            return true;
        }
        // Clean up variables if no match
        for (String var : parts) {
            Table.remove(var);
        }
        return false;
    }

    private boolean matchListPatternWithTail(ListPattern p, List<String> parts, int partsSize, BarleyList array, int arraySize) {
        // Set element variables
        final int lastPart = partsSize - 1;
        for (int i = 0; i < lastPart; i++) {
            Table.define(parts.get(i), array.getList().get(i));
        }
        // Set tail variable
        final BarleyList tail = new BarleyList(arraySize - partsSize + 1);
        for (int i = lastPart; i < arraySize; i++) {
            tail.set(i - lastPart, array.getList().get(i));
        }
        Table.define(parts.get(lastPart), tail);
        // Check optional condition
        if (optMatches(p)) {
            // Clean up will be provided after evaluate result
            return true;
        }
        // Clean up variables
        for (String var : parts) {
            Table.remove(var);
        }
        return false;
    }

    private boolean match(BarleyValue value, BarleyValue constant) {
        return value.equals(constant);
    }

    private boolean optMatches(Pattern pattern) {
        if (pattern.optCondition == null) return true;
        return pattern.optCondition.execute().toString() != "false";
    }

    private BarleyValue evalResult(AST s) {
        return s.execute();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("case ").append(expression).append(" {");
        for (Pattern p : patterns) {
            sb.append("\n  of ").append(p);
        }
        sb.append("\n}");
        return sb.toString();
    }

    public abstract static class Pattern implements Serializable {
        public AST result;
        public AST optCondition;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            if (optCondition != null) {
                sb.append(" when ").append(optCondition);
            }
            sb.append(": ").append(result);
            return sb.toString();
        }
    }

    public static class ConstantPattern extends Pattern {
        BarleyValue constant;

        public ConstantPattern(BarleyValue pattern) {
            this.constant = pattern;
        }

        @Override
        public String toString() {
            return constant.toString().concat(super.toString());
        }
    }

    public static class VariablePattern extends Pattern {
        public String variable;

        public VariablePattern(String pattern) {
            this.variable = pattern;
        }

        @Override
        public String toString() {
            return variable.concat(super.toString());
        }
    }

    public static class ListPattern extends Pattern {
        List<String> parts;

        public ListPattern() {
            this(new ArrayList<>());
        }

        ListPattern(List<String> parts) {
            this.parts = parts;
        }

        public void add(String part) {
            parts.add(part);
        }

        @Override
        public String toString() {
            final Iterator<String> it = parts.iterator();
            if (it.hasNext()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("[").append(it.next());
                while (it.hasNext()) {
                    sb.append(" :: ").append(it.next());
                }
                sb.append("]").append(super.toString());
                return sb.toString();
            }
            return "[]".concat(super.toString());
        }
    }

    public static class TuplePattern extends Pattern {
        private static final AST ANY = new AST() {
            @Override
            public BarleyValue execute() {
                return new BarleyNumber(1);
            }

            @Override
            public void visit(Optimization optimization) {

            }


            @Override
            public String toString() {
                return "_".concat(super.toString());
            }
        };
        public List<AST> values;

        public TuplePattern() {
            this(new ArrayList<AST>());
        }

        public TuplePattern(List<AST> parts) {
            this.values = parts;
        }

        public void addAny() {
            values.add(ANY);
        }

        public void add(AST value) {
            values.add(value);
        }

        @Override
        public String toString() {
            final Iterator<AST> it = values.iterator();
            if (it.hasNext()) {
                final StringBuilder sb = new StringBuilder();
                sb.append('(').append(it.next());
                while (it.hasNext()) {
                    sb.append(", ").append(it.next());
                }
                sb.append(')').append(super.toString());
                return sb.toString();
            }
            return "()".concat(super.toString());
        }
    }
}
