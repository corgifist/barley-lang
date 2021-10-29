package com.barley.ast;

import com.barley.patterns.ConstantPattern;
import com.barley.patterns.ListPattern;
import com.barley.patterns.Pattern;
import com.barley.patterns.VariablePattern;
import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.util.LinkedList;
import java.util.List;

public class BindAST implements AST {

    private AST left, right;

    public BindAST(AST left, AST right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public BarleyValue execute() {
        Pattern pattern = pattern(left);
        processPattern(pattern, right);
        return right.execute();
    }

    private void processPattern(Pattern pattern, AST r) {
        BarleyValue ast = r.execute();
        if (pattern instanceof ListPattern) {
            if (!(ast instanceof BarleyList)) throw new BarleyException("BadMatch", "no match of right-hand value: " + ast);
            ListPattern p = (ListPattern) pattern;
            LinkedList<BarleyValue> list = ((BarleyList) ast).getList();
            LinkedList<Pattern> patterns = pattern(p);
            for (int i = 0; i < list.size(); i++) {
                Pattern pattern1 = patterns.get(i);
                BarleyValue right = list.get(i);

                if (pattern1 instanceof VariablePattern) {
                    VariablePattern c = (VariablePattern) pattern1;
                    if (Table.isExists(c.getVariable())) processPattern(new ConstantPattern(Table.get(c.getVariable())), new ConstantAST(right));
                    else Table.set(c.getVariable(), right);
                } else if (pattern1 instanceof ConstantPattern) {
                    BarleyValue l = ((ConstantPattern) pattern1).getConstant();
                    if (right.equals(l));
                    else throw new BarleyException("BadMatch", "no match of right-hand value: " + ast);
                } else if (pattern1 instanceof ListPattern) {
                    if (!((right instanceof BarleyList))) throw new BarleyException("BadMatch", "no match of right-hand value: " + ast);
                    processPattern(pattern1, new ConstantAST(right));
                }
            }
            return;
        }
        if (pattern instanceof VariablePattern) {
            VariablePattern c = (VariablePattern) pattern;
            if (Table.isExists(c.getVariable())) processPattern(new ConstantPattern(Table.get(c.getVariable())), r);
            else Table.set(c.getVariable(), r.execute());
        } else if (pattern instanceof ConstantPattern) {
            BarleyValue l = ((ConstantPattern) pattern).getConstant();
            if (ast.equals(l));
            else throw new BarleyException("BadMatch", "no match of right-hand value: " + ast);
        }
    }

    private Pattern pattern(AST ast) {
        if (ast instanceof ExtractBindAST) {
            return new VariablePattern(ast.toString());
        } else if (ast instanceof ConstantAST) {
            return new ConstantPattern(ast.execute());
        } else if (ast instanceof BindAST) {
            return new ConstantPattern(ast.execute());
        } else if (ast instanceof ListAST) {
            return new ListPattern(((ListAST) ast).getArray());
        } else throw new BarleyException("BadMatch", "invalid pattern in bind ast");
    }

    private LinkedList<Pattern> pattern(ListPattern pattern) {
        LinkedList<AST> asts = pattern.getArr();
        LinkedList<Pattern> patterns = new LinkedList<>();
        for (AST ast : asts) {
            patterns.add(pattern(ast));
        }

        return patterns;
    }

    @Override
    public String toString() {
        return String.format("%s = %s", left, right);
    }
}
