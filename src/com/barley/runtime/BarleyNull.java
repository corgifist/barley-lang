package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BarleyNull implements BarleyValue {

    public BarleyNull() {
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "can't cast NULL to NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "can't cast NULL to NUMBER");
    }

    @Override
    public Object raw() {
        return null;
    }

    @Override
    public String toString() {
        return "#Null<" + hashCode() + ">";
    }
}
