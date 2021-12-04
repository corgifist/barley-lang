package com.barley.runtime;

import com.barley.Main;
import com.barley.memory.Storage;
import com.barley.utils.BarleyException;
import com.barley.utils.Pointers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;

public class BarleyPointer implements BarleyValue {

    private BarleyValue stored;
    private String pointer;

    public BarleyPointer(BarleyValue execute) {
        this.stored = execute;
        this.pointer = Integer.toHexString( Modules.getRandomNumber(0, 100000000));
        Pointers.put(this.toString(), stored);
        Storage.segment(this);
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "can't cast POINTER to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "can't cast POINTER to a NUMBER");
    }

    @Override
    public Object raw() {
        return stored;
    }

    public BarleyValue getStored() {
        return stored;
    }

    public void setStored(BarleyValue stored) {
        this.stored = stored;
    }

    @Override
    public String toString() {
        return pointer;
    }
}
