package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

public class BarleyString implements BarleyValue {

    private byte[] string;

    public BarleyString(byte[] string) {
        this.string = string;
    }

    public BarleyString(String s) {
        this(s.getBytes());
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarleyString that = (BarleyString) o;
        return Arrays.equals(string, that.string);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(string);
    }

    @Override
    public String toString() {
        return new String(string);
    }
}
