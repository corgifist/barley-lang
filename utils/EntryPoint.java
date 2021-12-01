package com.barley.utils;

import java.io.Serializable;

public class EntryPoint implements Serializable {
    private String name, method;

    public EntryPoint(String name, String method) {
        this.name = name;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "EntryPoint{" +
                "name='" + name + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}
