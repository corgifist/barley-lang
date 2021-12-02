package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyPID;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.ProcessTable;
import com.barley.runtime.Table;
import com.barley.utils.AST;

import java.io.Serializable;

public class ProcessCallAST implements AST, Serializable {

    private final int line;
    private final String current;
    public AST pid, expr;

    public ProcessCallAST(AST pid, AST expr, int line, String current) {
        this.pid = pid;
        this.expr = expr;
        this.line = line;
        this.current = current;
    }

    @Override
    public BarleyValue execute() {
        Table.set("Message", expr.execute());
        BarleyPID id = (BarleyPID) pid.execute();
        JavaFunctionAST ast = (JavaFunctionAST) ProcessTable.receives.get(id);
        return ast.execute();
    }

    @Override
    public void visit(Optimization optimization) {
        expr = optimization.optimize(expr);
    }

    @Override
    public String toString() {
        return pid + " ! " + expr;
    }
}
