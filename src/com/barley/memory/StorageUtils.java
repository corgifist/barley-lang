package com.barley.memory;

import com.barley.reflection.Reflection;
import com.barley.runtime.*;

import java.util.HashMap;
import java.util.Map;

public class StorageUtils {
    public static short size(BarleyValue value) {
        if (value instanceof BarleyNumber) {
            return 12;
        } else if (value instanceof BarleyString) {
            return 24;
        } else if (value instanceof BarleyPointer) {
            return 8;
        } else if (value instanceof BarleyClosure closure) {
            return 512;
        } else if (value instanceof Allocation p) {
            return (short) p.getAllocated();
        } else if (value instanceof BarleyList l) {
            return 24;
        } else if (value instanceof BarleyFunction) {
            return 48;
        }  else if (value instanceof BarleyAtom) {
            return 8;
        } else if (value instanceof BarleyReference) {
            return 128;
        } else if (value instanceof Reflection.ObjectValue) {
            return 328;
        }

        return 24;
    }
}
