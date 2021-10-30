package com.barley.runtime;

import com.barley.utils.BarleyException;
import com.barley.utils.Function;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class BarleyFunction implements BarleyValue, Function {

    private Function function;

    public BarleyFunction(Function function) {
        this.function = function;
    }

    public Function getFunction() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarleyFunction that = (BarleyFunction) o;
        return Objects.equals(function, that.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function);
    }

    @Override
    public String toString() {
        return function.toString();
    }
}
