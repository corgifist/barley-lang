package com.barley.runtime;

import java.util.HashMap;

public class Modules {

    private static HashMap<String, HashMap<String, UserFunction>> modules = new HashMap<>();

    public static void put(String name, HashMap<String, UserFunction> methods) {
        modules.put(name, methods);
    }

    public static HashMap<String, UserFunction> get(String name) {
        return modules.get(name);
    }

}
