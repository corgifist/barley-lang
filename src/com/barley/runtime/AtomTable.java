package com.barley.runtime;

import java.util.HashMap;
import java.util.Map;

public class AtomTable {

    private static Map<Integer, String> atoms = new HashMap<>();

    public static int put(String atom) {
        for (Map.Entry<Integer, String> entry : atoms.entrySet()) {
            if (entry.getValue().equals(atom)) return entry.getKey();
        }
        atoms.put(atoms.size() + 1, atom);
        return atoms.size();
    }

    public static String get(int pos) {
        return atoms.get(pos);
    }

}
