package com.barley.utils;

import com.barley.runtime.BarleyValue;

import java.io.Serializable;

public interface AST extends Serializable {

    BarleyValue execute();

}
