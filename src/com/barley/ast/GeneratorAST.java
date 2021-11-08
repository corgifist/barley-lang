package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyNumber;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.util.LinkedList;

public class GeneratorAST implements AST, Serializable {

    private AST gen;
    private String var;
    public AST iterable;

    public GeneratorAST(AST gen, String var, AST iterable) {
        this.gen = gen;
        this.var = var;
        this.iterable = iterable;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue value = iterable.execute();
        if (!(value instanceof BarleyValue)) throw new BarleyException("BadGenerator", "expected list as enumerable");
        BarleyList list = (BarleyList) value;
        LinkedList<BarleyValue> result = new LinkedList<>();
        int size = list.getList().size();
        for (int i = 0; i < size; i++) {
            BarleyValue obj = list.getList().get(i);
            Table.set(var, obj);
            Table.set("ITERATION", new BarleyNumber(i));
            BarleyValue res = (gen.execute());
            if (res.toString().equals("generator_skip")) continue;
            result.add(res);
        }
        Table.remove(var);
        Table.remove("ITERATION");
        return new BarleyList(result);
    }

    @Override
    public void visit(Optimization optimization) {
        iterable = optimization.optimize(iterable);
    }

    @Override
    public String toString() {
        return gen + " || " + var + " -> " + iterable;
    }
}
