package com.barley.utils;

import java.util.ArrayList;

public class Clause {

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

    @Override
    public String toString() {
        return "Clause{" +
                "args=" + args +
                ", guard=" + guard +
                ", result=" + result +
                '}';
    }
}
