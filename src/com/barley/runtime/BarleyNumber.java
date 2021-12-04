package com.barley.runtime;

import com.barley.memory.Storage;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class BarleyNumber implements BarleyValue, Serializable {

    private BigDecimal number;

    public static BarleyNumber fromBoolean(boolean bool) {
        return new BarleyNumber(bool ? 1 : 0);
    }

    public static BarleyNumber of (Number val) {
        return of(val.doubleValue());
    }

    public static BarleyNumber of (double val) {
        return new BarleyNumber(val);
    }

    public BarleyNumber(BigDecimal number) {
        this.number = number; Storage.segment(this);
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
    public Object raw() {
        if (this.toString().contains(".")) return number.doubleValue();
        else return number.intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarleyNumber that = (BarleyNumber) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return number.remainder(BigDecimal.valueOf(1)).equals(BigDecimal.valueOf(0.0)) ? String.valueOf(number.longValue()) : number.toString();
    }
}
