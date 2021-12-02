package com.barley.reflection;

import java.lang.reflect.Method;
import java.util.HashMap;

public class MethodClause {
    private HashMap<Integer, Method> overloads;

    public MethodClause(HashMap<Integer, Method> overloads) {
        this.overloads = overloads;
    }

    public HashMap<Integer, Method> getOverloads() {
        return overloads;
    }

    public void setOverloads(HashMap<Integer, Method> overloads) {
        this.overloads = overloads;
    }

    public Method get(Object key) {
        return overloads.get(key);
    }

    public Method put(Integer key, Method value) {
        return overloads.put(key, value);
    }

    @Override
    public String toString() {
        return overloads.toString();
    }
}
