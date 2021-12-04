package com.barley.memory;

import com.barley.runtime.BarleyValue;

public class Storage {

    private static int left = 749000;

    public static void free(BarleyValue obj) {
        left += StorageUtils.size(obj);
    }

    public static void segment(BarleyValue obj) {
        left -= StorageUtils.size(obj);
    }

    public static void reset() { left = 31999; }

    public static int left() {
        return left;
    }

}
