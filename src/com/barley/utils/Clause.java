package com.barley.utils;

import com.barley.optimizations.Optimization;

import java.io.Serializable;
import java.util.ArrayList;

public class Clause implements Serializable {

    private ArrayList<AST> args;
    private AST guard;
    private AST result;

    public Clause(ArrayList<AST> args, AST guard, AST result) {
        this.args = args;
        this.guard = guard;
        this.result = result;
    }

    public ArrayList<AST> getArgs() {
        return args;
    }

    public void setArgs(ArrayList<AST> args) {
        this.args = args;
    }

    public AST getGuard() {
        return guard;
    }

    public void setGuard(AST guard) {
        this.guard = guard;
    }

    public AST getResult() {
        return result;
    }

    public void setResult(AST result) {
        this.result = result;
    }

    public Clause optimize(Optimization opt) {
        result = opt.optimize(result);
        return this;
    }

    @Override
    public String toString() {
        return guard == null ? String.format("%s -> %s", args, result) : String.format("%s when %s -> %s", args, guard, result);
    }
}
