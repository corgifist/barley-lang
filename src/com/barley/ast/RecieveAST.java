package com.barley.ast;

import com.barley.runtime.*;
import com.barley.utils.AST;

public class RecieveAST implements AST {

    private AST pid, body;
    private BarleyPID p;

    public RecieveAST(AST pid, AST body) {
        this.pid = pid;
        this.body = body;
        this.p = (BarleyPID) pid.execute();
    }

    @Override
    public BarleyValue execute() {
        ProcessTable.receives.put(p, new JavaFunctionAST(args -> {
            Table.set("Rest", ProcessTable.get(p));
            ProcessTable.put(p, body.execute());
            return ProcessTable.get(p);
        }, new BarleyValue[]{}));
        return p;
    }

    @Override
    public String toString() {
        return "recieve " + pid + " -> " + body;
    }
}