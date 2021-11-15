package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class BarleyAtom implements BarleyValue, Serializable {

    private String atom;

    public BarleyAtom(String atom) {
        this.atom = atom;
    }

    public BarleyAtom(int pos) {
        this(AtomTable.get(pos));
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarleyAtom that = (BarleyAtom) o;
        return Objects.equals(atom, that.atom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(atom);
    }

    @Override
    public String toString() {
        return atom;
    }
}
