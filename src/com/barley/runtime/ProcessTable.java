package com.barley.runtime;

import com.barley.utils.AST;

import java.util.HashMap;

public class ProcessTable {

    public static HashMap<BarleyPID, BarleyValue> storage = new HashMap<>();
    public static HashMap<BarleyPID, AST> receives = new HashMap<>();

    public static void put(BarleyPID pid, BarleyValue val) {
        storage.put(pid, val);
    }

    public static void put(BarleyPID pid) {
        put(pid, new BarleyNumber(0));
    }

    public static BarleyValue get(BarleyPID pid) {
        BarleyValue result = storage.get(pid);
        return result;
    }

    public static void dump() {
        System.out.println(storage);
        System.out.println(receives);
    }

}
