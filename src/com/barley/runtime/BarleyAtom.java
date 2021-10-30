package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class BarleyAtom implements BarleyValue {

    private int pos;

    public BarleyAtom(int pos) {
        this.pos = pos;
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "Cannot cast ATOM to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "Cannot cast ATOM to a NUMBER");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BarleyAtom that = (BarleyAtom) o;
        return pos == that.pos;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

    @Override
    public String toString() {
        return AtomTable.get(pos);
    }
}
