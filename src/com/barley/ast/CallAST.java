package com.barley.ast;

import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyValue;
import com.barley.utils.*;

import java.util.ArrayList;

public class CallAST implements AST {

    private AST obj;
    private ArrayList<AST> args;

    public CallAST(AST obj, ArrayList<AST> args) {
        this.obj = obj;
        this.args = args;
    }

    @Override
    public BarleyValue execute() {
        BarleyValue[] arguments = new BarleyValue[args.size()];
        for (int i = 0; i < args.size(); i++) {
            AST node = args.get(i);
            arguments[i] = node.execute();
        }
        BarleyValue temporal = obj.execute();
        if (!(temporal instanceof BarleyFunction)) throw new BarleyException("BadArg", "expected callable object, but got not callable");
        BarleyFunction function = (BarleyFunction) temporal;
        Function fun = function.getFunction();
        BarleyValue result = null;
        try {
            CallStack.enter(obj.toString(), fun);
            result = fun.execute(arguments);
            CallStack.exit();
        } finally {
            return result;
        }
    }

    @Override
    public String toString() {
        return obj.toString() + args;
    }
}
