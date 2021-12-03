package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyPointer;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;
import com.barley.utils.Pointers;

import java.io.Serializable;

public class PointShiftAST implements AST, Serializable {

    private AST pointer, value;

    public PointShiftAST(AST pointer, AST value) {
        this.pointer = pointer;
        this.value = value;
    }

    @Override
    public BarleyValue execute() {
        BarleyPointer point = (BarleyPointer) pointer.execute();
        Pointers.put(point.toString(), value.execute());
        return point;
    }

    @Override
    public void visit(Optimization optimization) {
        pointer.visit(optimization);
        value.visit(optimization);
    }

    @Override
    public String toString() {
        return pointer + " >> " + value;
    }
}
