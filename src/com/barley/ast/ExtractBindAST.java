package com.barley.ast;

import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;

public class ExtractBindAST implements AST {

    private String constant;

    public ExtractBindAST(String constant) {
        this.constant = constant;
    }

    @Override
    public BarleyValue execute() {
        return Table.get(constant);
    }

    @Override
    public String toString() {
        return constant;
    }
}
