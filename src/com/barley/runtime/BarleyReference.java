package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BarleyReference implements BarleyValue {

    private Object ref;

    public BarleyReference(Object ref) {
        this.ref = ref;
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "Cannot cast REFERENCE to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "Cannot cast REFERENCE to a NUMBER");
    }

    public Object getRef() {
        return ref;
    }

    @Override
    public String toString() {
        return "#Reference<" + hashCode() + ">";
    }
}
