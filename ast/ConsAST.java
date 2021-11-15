package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;
import java.util.LinkedList;

public class ConsAST implements AST, Serializable {

    public AST left, right;

    public ConsAST(AST left, AST right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public BarleyValue execute() {
        LinkedList<BarleyValue> list = new LinkedList<>();
        list.add(left.execute());
        list.add(right.execute());
        return new BarleyList(list);
    }

    @Override
    public void visit(Optimization optimization) {
        left = optimization.optimize(left);
        right = optimization.optimize(right);
    }


    public AST getLeft() {
        return left;
    }

    public AST getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left + " | " + right;
    }
}
