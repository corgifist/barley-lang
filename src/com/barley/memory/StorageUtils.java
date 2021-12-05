package com.barley.memory;

import com.barley.reflection.Reflection;
import com.barley.runtime.*;

public class StorageUtils {
    public static short size(BarleyValue value) {
        if (value instanceof BarleyNumber) {
            return 12;
        } else if (value instanceof BarleyString) {
            return 24;
        } else if (value instanceof BarleyPointer) {
            return 8;
        } else if (value instanceof Allocation p) {
            return (short) p.getAllocated();
        } else if (value instanceof BarleyList l) {
            short buffer = 0;
            for (BarleyValue val : l.getList()) {
                buffer += size(val);
            }
            return buffer;
        } else if (value instanceof BarleyFunction) {
            return 48;
        } else if (value instanceof BarleyAtom) {
            return 8;
        } else if (value instanceof BarleyReference) {
            return 128;
        } else if (value instanceof Reflection.ObjectValue) {
            return 328;
        }

        return 24;
    }
}
