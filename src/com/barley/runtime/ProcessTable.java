package com.barley.runtime;

import java.util.HashMap;

public class ProcessTable {

    public static HashMap<BarleyPID, BarleyValue> storage = new HashMap<>();

    public static void put(BarleyPID pid, BarleyValue val) {
        storage.put(pid, val);
    }

    public static void put(BarleyPID pid) {
        put(pid, new BarleyAtom(AtomTable.put("pid")));
    }

    public static BarleyValue get(BarleyPID pid) {
        return storage.get(pid);
    }

}
