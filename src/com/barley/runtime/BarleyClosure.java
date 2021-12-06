package com.barley.runtime;

import com.barley.utils.Function;

import java.io.Serializable;

public class BarleyClosure extends BarleyFunction implements Serializable {

    private Table.Scope scope;

    public BarleyClosure(Function function) {
        super(function);
        this.scope = Table.scope;
    }

    @Override
    public BarleyValue execute(BarleyValue... args) {
        Table.Scope old = Table.scope;
        Table.scope = scope;
        BarleyValue result = super.execute(args);
        Table.scope = old;
        return result;
    }

    public Table.Scope getScope() {
        return scope;
    }

    @Override
    public String toString() {
        return "#Closure<" + hashCode() + ">";
    }
}
