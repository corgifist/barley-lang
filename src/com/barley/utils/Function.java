package com.barley.utils;

import com.barley.runtime.BarleyValue;

import java.io.Serializable;

public interface Function extends Serializable {
    BarleyValue execute(BarleyValue... args);
}
