package com.barley.ast;

import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyValue;
import com.barley.utils.AST;

import java.io.Serializable;
import java.util.LinkedList;

public class ListAST implements AST, Serializable {

    private LinkedList<AST> array;

    public ListAST(LinkedList<AST> array) {
        this.array = array;
    }

    @Override
    public BarleyValue execute() {
        LinkedList<BarleyValue> arr = new LinkedList<>();
        for (AST ast : array) {
            arr.add(ast.execute());
        }
        return new BarleyList(arr);
    }

    public LinkedList<AST> getArray() {
        return array;
    }

    @Override
    public String toString() {
        return array.toString();
    }
}
