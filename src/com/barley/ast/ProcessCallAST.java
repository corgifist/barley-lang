package com.barley.ast;

import com.barley.runtime.*;
import com.barley.utils.AST;

public class ProcessCallAST implements AST {

    private AST pid, expr;

    public ProcessCallAST(AST pid, AST expr) {
        this.pid = pid;
        this.expr = expr;
    }

    @Override
    public BarleyValue execute() {
        Table.set("Message", expr.execute());
        BarleyPID id = (BarleyPID) pid.execute();
        JavaFunctionAST ast = (JavaFunctionAST) ProcessTable.receives.get(id);
        return ast.execute();
    }

    @Override
    public String toString() {
        return pid + " ! " + expr;
    }
}
