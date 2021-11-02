package com.barley.ast;

import com.barley.runtime.*;
import com.barley.utils.AST;

import java.io.Serializable;

public class RecieveAST implements AST, Serializable {

    private AST pid, body;
    private BarleyPID p;

    public RecieveAST(AST pid, AST body) {
        this.pid = pid;
        this.body = body;
        this.p = (BarleyPID) pid.execute();
        ProcessTable.receives.put(p, new JavaFunctionAST(args -> {
            Table.set("Rest", ProcessTable.get(p));
            ProcessTable.put(p, body.execute());
            Table.remove("Rest");
            return ProcessTable.get(p);
        }, new BarleyValue[]{}));
    }

    @Override
    public BarleyValue execute() {
        return p;
    }

    @Override
    public String toString() {
        return "recieve " + pid + " -> " + body;
    }
}
