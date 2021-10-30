package com.barley.runtime;

import com.barley.utils.BarleyException;
import com.barley.utils.Function;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BarleyFunction implements BarleyValue, Function {

    private UserFunction function;

    public BarleyFunction(UserFunction function) {
        this.function = function;
    }

    public UserFunction getFunction() {
        return function;
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "Cannot cast FUNCTION to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "Cannot cast FUNCTION to a NUMBER");
    }

    @Override
    public BarleyValue execute(BarleyValue... args) {
        return function.execute(args);
    }

    @Override
    public String toString() {
        return function.toString();
    }
}
