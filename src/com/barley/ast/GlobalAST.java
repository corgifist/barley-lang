package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;

import java.io.Serializable;

public class GlobalAST implements AST, Serializable {

    private AST global;

    public GlobalAST(AST global) {
        this.global = global;
    }

    @Override
    public BarleyValue execute() {
        global.execute();
        return new BarleyAtom("ok");
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return "global " + global;
    }
}
