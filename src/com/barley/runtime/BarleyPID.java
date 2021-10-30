package com.barley.runtime;

import com.barley.utils.BarleyException;
import com.barley.utils.PidValues;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class BarleyPID implements BarleyValue {

    private PidValues id;

    public BarleyPID(PidValues id) {
        this.id = id;
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "Cannot cast STRING to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "Cannot cast STRING to a NUMBER");
    }

    public PidValues getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarleyPID barleyPID = (BarleyPID) o;
        return Objects.equals(id, barleyPID.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "#PID<" + id + ">";
    }
}
