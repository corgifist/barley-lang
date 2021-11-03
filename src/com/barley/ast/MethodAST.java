package com.barley.ast;

import com.barley.parser.Parser;
import com.barley.runtime.AtomTable;
import com.barley.runtime.BarleyAtom;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.UserFunction;
import com.barley.utils.AST;

import java.io.Serializable;

public class MethodAST implements AST, Serializable {

    private Parser parser;
    private UserFunction method;
    private String name;

    public MethodAST(Parser parser, UserFunction method, String name) {
        this.parser = parser;
        this.method = method;
        this.name = name;
    }

    @Override
    public BarleyValue execute() {
        parser.methods.put(name, method);
        return new BarleyAtom("ok");
    }

    @Override
    public String toString() {
        return name + method.toString();
    }
}
