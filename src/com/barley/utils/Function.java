package com.barley.utils;

import com.barley.runtime.BarleyValue;
import com.sun.jdi.Value;

public interface Function {
    BarleyValue execute(BarleyValue... args);
}
