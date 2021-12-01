package com.barley.ast;

import com.barley.Main;
import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;

import java.io.Serializable;

public class ExtractBindAST implements AST, Serializable {

    private final int line;
    private final String current;
    private String constant;

    public ExtractBindAST(String constant, int line, String current) {
        this.constant = constant;
        this.line = line;
        this.current = current;
    }

    @Override
    public BarleyValue execute() {
        if (!Table.isExists(constant)) Main.error("UnboundVar", "unbound var '" + constant + "'", line, current);
        return Table.get(constant);
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return constant;
    }
}
