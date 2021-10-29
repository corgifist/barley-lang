package com.barley.ast;

import com.barley.patterns.ConstantPattern;
import com.barley.patterns.Pattern;
import com.barley.patterns.VariablePattern;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

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
        } else throw new BarleyException("BadMatch", "invalid pattern in bind ast");
    }
}
