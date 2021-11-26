package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;

public class TernaryAST implements AST, Serializable {

    public AST term, left, right;

    public TernaryAST(AST term, AST left, AST right) {
        this.term = term;
        this.left = left;
        this.right = right;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue t = term.execute();
        if (t.toString().equals("true")) return left.execute();
        else return right.execute();
    }

    @Override
    public void visit(Optimization optimization) {
        term = optimization.optimize(term);
        left = optimization.optimize(left);
        right = optimization.optimize(right);
    }

    @Override
    public String toString() {
        return String.format("%s ? %s :: %s", term, left, right);
    }
}
