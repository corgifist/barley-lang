package com.barley.runtime;

import com.barley.memory.Storage;
import com.barley.reflection.Reflection;
import com.barley.utils.Arguments;
import com.barley.utils.Function;
import com.barley.utils.Pointers;

import java.util.ArrayList;
import java.util.HashMap;

public class Externals {

    private static HashMap<String, Function> exts = new HashMap<>();

    static {
        Externals.put("free", args -> {
            Arguments.check(1, args.length);
            Storage.free(args[0]);
            Pointers.remove(args[0].toString());
            return new BarleyAtom("ok");
        });
        Externals.put("allocate", args -> {
            Arguments.check(0, args.length);
            BarleyPointer ptr = new BarleyPointer(new BarleyNumber(0));
            Pointers.put(ptr.toString(), new BarleyList());
            Storage.segment(420);
            return ptr;
        });
    }

    public static Function get(Object key) {
        return exts.get(key);
    }

    public static Function put(String key, Function value) {
        return exts.put(key, value);
    }

    public static boolean containsKey(Object key) {
        return exts.containsKey(key);
    }

    public static void clear() {
        exts.clear();
    }
}
