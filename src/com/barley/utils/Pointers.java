package com.barley.utils;

import com.barley.runtime.BarleyValue;

import java.util.HashMap;

public class Pointers {
    private static HashMap<String, BarleyValue> pointers = new HashMap<>();

    public static BarleyValue get(Object key) {
        return pointers.get(key);
    }

    public static BarleyValue put(String key, BarleyValue value) {
        return pointers.put(key, value);
    }

    public static void clear() {
        pointers.clear();
    }

    public static HashMap<String, BarleyValue> getPointers() {
        return pointers;
    }
}
