package com.barley.ast;

import com.barley.Main;
import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyPointer;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;
import com.barley.utils.Pointers;

import java.io.Serializable;

public class UnPointAST implements AST, Serializable {

    private final int line;
    private final String current;
    private AST ast;

    public UnPointAST(AST ast, int line, String current) {
        this.ast = ast;
        this.line = line;
        this.current = current;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue execute = ast.execute();
        if (!(execute instanceof BarleyPointer))
            Main.error("BadPointer", "expected POINTER as pointer, got '" + execute.toString() + "'", line, current);
        return Pointers.get(execute.toString());
    }

    @Override
    public void visit(Optimization optimization) {
        ast.visit(optimization);
    }

    @Override
    public String toString() {
        return "##" +  ast.toString();
    }
}
