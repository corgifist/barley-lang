package com.barley.ast;

import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Modules;
import com.barley.utils.AST;

public class RemoteAST implements AST {

    private AST module, target;

    public RemoteAST(AST module, AST target) {
        this.module = module;
        this.target = target;
    }

    @Override
    public BarleyValue execute() {
        return new BarleyFunction(Modules.get(module.execute().toString()).get(target.execute().toString()));
    }

    @Override
    public String toString() {
        return String.format("%s:%s", module, target);
    }
}
