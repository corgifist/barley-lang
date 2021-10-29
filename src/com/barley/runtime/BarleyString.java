package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BarleyString implements BarleyValue {

    private byte[] string;

    private BarleyString(String str) {
        this(str.getBytes());
    }

    public BarleyString(byte[] string) {
        this.string = string;
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "Cannot cast STRING to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "Cannot cast STRING to a NUMBER");
    }

    @Override
    public String toString() {
        return new String(string);
    }
}
