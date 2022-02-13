package com.barley.ast;

import com.barley.Main;
import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Modules;
import com.barley.utils.AST;
import com.barley.utils.Function;

import java.io.Serializable;
import java.util.HashMap;

public class RemoteAST implements AST, Serializable {

    private final String current;
    private final int line;
    private AST module, target;

    public RemoteAST(AST module, AST target, int line, String current) {
        this.module = module;
        this.target = target;
        this.line = line;
        this.current = current;
    }

    @Override
    public BarleyValue execute() {
        String m = module.execute().toString();
        String t = target.execute().toString();
        if (!(Modules.isExists(m)))
            Main.error("BadArg", "module '" + m + "'  is not compiled or doesn't exists", line, current);
        Function a = Modules.get(module.execute().toString()).get(t);
        if (a == null)
            Main.error("BadArg", "module '" + m + "' exists but function '" + t + "' is not\nmaybe you mean '" + getSimilarFun(m, t) + "'?", line, current);
        return new BarleyFunction(a);
    }

    private String getSimilarFun(String module, String word) {
        HashMap<String, Integer> calculations = new HashMap<>();
        var functions = Modules.get(module);
        for (var entry : functions.entrySet()) {
            int count = 0;
            String base = entry.getKey();
            try {
                for (int i = 0; i < word.length(); i++) {
                    if (word.charAt(i) == base.charAt(i)) {
                        count++;
                    }
                }
            } catch (Exception ex) {
                // skip
            }
            calculations.put(base, count);
        }

        String result = "";
        int oldCount = 0;
        for (var entry : calculations.entrySet()) {
            String fword = entry.getKey();
            int count = entry.getValue();
            if (count > oldCount) result = fword;
        }
        return result;
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return String.format("%s:%s", module, target);
    }
}
