package com.barley.ast;

import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;

public class TernaryAST implements AST, Serializable {

    AST term, left, right;

    public TernaryAST(AST term, AST left, AST right) {
        this.term = term;
        this.left = left;
        this.right = right;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue t = term.execute();
        BarleyValue l = left.execute();
        BarleyValue r = right.execute();
        if (t.toString().equals("true")) return l;
        else return r;
    }

    @Override
    public String toString() {
        return String.format("%s ? %s :: %s", term, left, right);
    }
}
