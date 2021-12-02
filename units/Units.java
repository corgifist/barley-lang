package com.barley.units;

import java.util.HashMap;

public class Units {
    private static HashMap<String, UnitBase> units = new HashMap<>();

    public static void put(String str, UnitBase b) {
        units.put(str, b);
    }

    public static UnitBase get(String str) {
        return units.get(str);
    }
}
