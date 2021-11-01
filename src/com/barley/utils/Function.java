package com.barley.utils;

import com.barley.runtime.BarleyValue;
import com.sun.jdi.Value;

import java.io.Serializable;

public interface Function extends Serializable {
    BarleyValue execute(BarleyValue... args);
}
