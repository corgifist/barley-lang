package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyFunction;
import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.CallStack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CallAST implements AST, Serializable {

    private AST obj;
    private ArrayList<AST> args;

    public CallAST(AST obj, ArrayList<AST> args) {
        this.obj = obj;
        this.args = args;
    }

    @Override
    public BarleyValue execute() {
        List<BarleyValue> arg = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            AST node = args.get(i);
            if (node instanceof UnPackAST pack) {
                LinkedList<BarleyValue> list = (((BarleyList) pack.getAst().execute()).getList());
                arg.addAll(list);
                break;
            } else arg.add(node.execute());
        }
        BarleyValue[] arguments = arg.toArray(new BarleyValue[] {});
        BarleyValue temporal = obj.execute();
        BarleyFunction function = (BarleyFunction) temporal;
        BarleyValue result;
        CallStack.enter(obj.toString() + Arrays.toString(arguments), function);
        result = function.execute(arguments);
        CallStack.exit();
        return result;
    }

    @Override
    public void visit(Optimization optimization) {
        ArrayList<AST> argss = new ArrayList<>();
        for (AST node : args) {
            argss.add(optimization.optimize(node));
        }
        args.clear();
        args.addAll(argss);
    }

    @Override
    public String toString() {
        return obj.toString() + args;
    }
}
