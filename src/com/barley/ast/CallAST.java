package com.barley.ast;

import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyValue;
import com.barley.utils.*;

import java.io.Serializable;
import java.util.ArrayList;

public class CallAST implements AST, Serializable {

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
        BarleyFunction function = (BarleyFunction) temporal;
        BarleyValue result = null;
        CallStack.enter(obj.toString(), function);
        result = function.execute(arguments);
        CallStack.exit();
        return result;
    }

    @Override
    public String toString() {
        return obj.toString() + args;
    }
}
