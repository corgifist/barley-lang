package com.barley.runtime;

import com.barley.memory.Storage;
import com.barley.utils.BarleyException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class BarleyList implements BarleyValue, Serializable {

    private LinkedList<BarleyValue> list;

    public BarleyList(LinkedList<BarleyValue> list) {
        this.list = list; Storage.segment(this);
    }

    public BarleyList(BarleyValue... values) {
        List<BarleyValue> vals = List.of(values);
        LinkedList<BarleyValue> res = new LinkedList<>();
        for (BarleyValue val : vals) {
            res.add(val);
        }
        this.list = res;
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
    public Object raw() {
        return list.toArray();
    }

    @Override
    public String toString() {
        if (list.size() >= 10) {
            List<BarleyValue> rest = list.subList(1, 10);
            StringBuilder buffer = new StringBuilder();
            buffer.append("[");
            int i = 1;
            for (BarleyValue val : rest) {
                buffer.append(i == 10 ? val.toString() : val + ", ");
                i++;
            }
            buffer.append("...");
            buffer.append("]");
            return buffer.toString();
        }
        else return list.toString();
    }
}
