package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyPID;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.ProcessTable;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.BarleyException;
import com.barley.utils.CallStack;

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
            BarleyValue previous = ProcessTable.get(p);
            try {
                ProcessTable.put(p, body.execute());
            } catch (BarleyException ex) {
                System.out.printf("** ERROR REPORT IN THREAD %s: %s\n", p, ex.getText());
                int count = CallStack.getCalls().size();
                if (count == 0) return previous;
                System.out.println(String.format("\nCall stack was:"));
                for (CallStack.CallInfo info : CallStack.getCalls()) {
                    System.out.println("    " + count + ". " + info);
                    count--;
                }
            }
            Table.remove("Rest");
            return ProcessTable.get(p);
        }, new BarleyValue[]{}));
    }

    @Override
    public BarleyValue execute() {
        return p;
    }

    @Override
    public void visit(Optimization optimization) {

    }

    @Override
    public String toString() {
        return "recieve " + pid + " -> " + body;
    }
}
