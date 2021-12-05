package com.barley.runtime;

import com.barley.memory.Allocation;
import com.barley.memory.Storage;
import com.barley.memory.StorageUtils;
import com.barley.reflection.Reflection;
import com.barley.utils.Arguments;
import com.barley.utils.BarleyException;
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
        Externals.put("alloc", args -> {
            Arguments.check(1, args.length);
            BarleyPointer ptr = new BarleyPointer();
            Pointers.put(ptr.toString(), new Allocation(args[0].asInteger().intValue()));
            Storage.segment(args[0].asInteger().intValue());
            return ptr;
        });
        Externals.put("altlst", args -> {
            Arguments.check(1, args.length);
            BarleyPointer ptr = (BarleyPointer) args[0];
            Allocation alc = (Allocation) Pointers.get(ptr.toString());
            return alc.toList();
        });
        Externals.put("alinst", args -> {
            Arguments.check(2, args.length);
            BarleyPointer ptr = (BarleyPointer) args[0];
            Allocation alc = (Allocation) Pointers.get(ptr.toString());
            alc.segment(args[1]);
            //System.out.println("alloc: " + alc.getAllocated() + "\ndefalloc: " + alc.getDefaultAlloc());
            if (alc.getAllocated() < 0)
                throw new BarleyException("SegmentationFault", "segmentation fault");
            alc.getList().add(args[1]);
            Pointers.put(ptr.toString(), alc);
            return alc;
        });
        Externals.put("alcpy", args -> {
            Arguments.check(1,args.length);
            BarleyPointer ptr = new BarleyPointer();
            Allocation old = (Allocation) Pointers.get(args[0].toString());
            Allocation alc = new Allocation(old.getDefaultAlloc());
            alc.setAllocated(old.getAllocated());
            alc.setDefaultAlloc(old.getDefaultAlloc());
            alc.setList(old.getList());
            Pointers.put(ptr.toString(), alc);
            return ptr;
        });

        Externals.put("alclr", args -> {
            Arguments.check(1, args.length);
            BarleyPointer ptr = (BarleyPointer) args[0];
            Allocation alc = (Allocation) Pointers.get(ptr.toString());
            alc.clear();
            return alc;
        });
        Externals.put("alcmp", args -> {
            Arguments.check(2, args.length);
            BarleyPointer ptr = (BarleyPointer) args[0];
            Allocation alc = (Allocation) Pointers.get(ptr.toString());
            BarleyPointer ptr2 = (BarleyPointer) args[1];
            Allocation alc2 = (Allocation) Pointers.get(ptr2.toString());
            if (alc.getDefaultAlloc() != alc2.getDefaultAlloc()) return new BarleyAtom("false");
            if (alc.getAllocated() != alc2.getAllocated()) return new BarleyAtom("false");
            if (!alc.getList().equals(alc2.getList())) return new BarleyAtom("false");
            return new BarleyAtom("true");
        });
        Externals.put("realloc", args -> {
            Arguments.check(1,args.length);
            BarleyPointer ptr = (BarleyPointer) args[0];
            Allocation alc = (Allocation) Pointers.get(ptr.toString());
            BarleyPointer newPtr = new BarleyPointer();
            Pointers.put(newPtr.toString(), alc);
            return newPtr;
        });
        Externals.put("nullptr", args -> {
            Arguments.check(0, args.length);
            BarleyPointer ptr = new BarleyPointer();
            Pointers.put(ptr.toString(), new BarleyNull());
            return ptr;
        });
        Externals.put("sizeof", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(StorageUtils.size(args[0]));
        });
        Externals.put("alszs", args -> new BarleyNumber(Storage.left()));
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
