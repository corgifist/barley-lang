package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.AtomTable;
import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Modules;
import com.barley.utils.AST;
import com.barley.utils.Function;

import java.io.Serializable;
import java.util.HashMap;

public class CompileAST implements AST, Serializable {

    private String module;
    private HashMap<String, Function> methods;

    public CompileAST(String module, HashMap<String, Function> methods) {
        this.module = module;
        this.methods = methods;
    }

    @Override
    public BarleyValue execute() {
        Modules.put(module, methods);
        return new BarleyAtom(AtomTable.put("ok"));
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return "";
    }
}
