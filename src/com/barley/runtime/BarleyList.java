package com.barley.runtime;

import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;

public class BarleyList implements BarleyValue {

    private LinkedList<BarleyValue> list;

    public BarleyList(LinkedList<BarleyValue> list) {
        this.list = list;
    }

    public BarleyList(int size) {
        LinkedList<BarleyValue> result = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            result.add(null);
        }
        this.list = result;
    }

    public void set(int index, BarleyValue value) {
        list.set(index, value);
    }

    public LinkedList<BarleyValue> getList() {
        return list;
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "Cannot cast LIST to a NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "Cannot cast LIST to a NUMBER");
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
