package com.barley.patterns;

import com.barley.utils.AST;

import java.io.Serializable;
import java.util.LinkedList;

public class ListPattern extends Pattern implements Serializable {

    private LinkedList<AST> arr;

    public ListPattern(LinkedList<AST> arr) {
        this.arr = arr;
    }

    public LinkedList<AST> getArr() {
        return arr;
    }

    @Override
    public String toString() {
        return arr.toString();
    }
}
