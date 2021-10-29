package com.barley.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BarleyNumber implements BarleyValue {

    BigDecimal number;

    public BarleyNumber(BigDecimal number) {
        this.number = number;
    }

    public BarleyNumber(double v) {
        this(BigDecimal.valueOf(v));
    }

    @Override
    public BigInteger asInteger() {
        return number.toBigInteger();
    }

    @Override
    public BigDecimal asFloat() {
        return number;
    }

    @Override
    public String toString() {
        return number.remainder(BigDecimal.valueOf(1)).equals(BigDecimal.valueOf(0.0)) ? String.valueOf(number.longValue()) : number.toString();
    }
}
