package com.barley.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BarleyNumber implements BarleyValue {

    BigDecimal number;

    private BarleyNumber(double value) {
        number = BigDecimal.valueOf(value);
    }

    private BarleyNumber(long value) {
        number = BigDecimal.valueOf(value);
    }

    public BarleyNumber(BigDecimal number) {
        this.number = number;
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
        return number.toString();
    }
}
