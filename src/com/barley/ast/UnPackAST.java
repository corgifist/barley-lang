package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyString;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;

public class UnPackAST implements AST, Serializable {

    public AST ast;

    public UnPackAST(AST ast) {
        this.ast = ast;
    }

    @Override
    public BarleyValue execute() {
        return new BarleyString("unpack");
    }

    @Override
    public void visit(Optimization optimization) {

    }

    public AST getAst() {
        return ast;
    }

    @Override
    public String toString() {
        return "unpack_expr " + ast;
    }
}
