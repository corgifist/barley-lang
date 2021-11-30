package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyString;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

public class PackAST implements AST {

    public String name;

    public PackAST(String name) {
        this.name = name;
    }

    @Override
    public BarleyValue execute() {
        return new BarleyString("pack");
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return "pack_expr " + name;
    }
}
