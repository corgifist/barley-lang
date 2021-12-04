package com.barley.memory;

import com.barley.runtime.BarleyValue;
import com.barley.utils.BarleyException;

public class Storage {

    private static int left = 749000;

    public static void free(BarleyValue obj) {
        left += StorageUtils.size(obj);
    }

    public static void segment(BarleyValue obj) {
        left -= StorageUtils.size(obj);
        if (left <= 0) throw new BarleyException("SegmentationFault", "segmentation fault, last allocation: '#Allocation<" + obj + ":" + StorageUtils.size(obj) + ">'");
    }

    public static void reset() { left = 749000; }

    public static int left() {
        return left;
    }

}
