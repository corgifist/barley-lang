package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;

import java.io.Serializable;

public class UnStrictAST implements AST, Serializable {
    @Override
    public BarleyValue execute() {
        Table.strict = false;
        return new BarleyAtom("ok");
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return "UnStrictAST{}";
    }
}
