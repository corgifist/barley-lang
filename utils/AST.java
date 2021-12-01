package com.barley.utils;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyValue;

import java.io.Serializable;

public interface AST extends Serializable {

    BarleyValue execute();

    void visit(Optimization optimization);

}
