package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Modules;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;
import com.barley.utils.Function;

import java.io.Serializable;

public class RemoteAST implements AST, Serializable {

    private AST module, target;

    public RemoteAST(AST module, AST target) {
        this.module = module;
        this.target = target;
    }

    @Override
    public BarleyValue execute() {
        String m = module.execute().toString();
        String t = target.execute().toString();
        if (!(Modules.isExists(m)))
            throw new BarleyException("BadArg", "module '" + m + "' is not compiled or doesn't exists");
        Function a = Modules.get(module.execute().toString()).get(t);
        if (a == null)
            throw new BarleyException("Undef", "module '" + m + "' exists but function '" + t + "' doesn't");
        return new BarleyFunction(a);
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return String.format("%s:%s", module, target);
    }
}
