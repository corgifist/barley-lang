package com.barley.memory;

import com.barley.runtime.BarleyList;
import com.barley.runtime.BarleyValue;
import com.barley.utils.BarleyException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class Allocation implements BarleyValue {

    private LinkedList<BarleyValue> list;
    private int allocated, defaultAlloc;

    public Allocation(LinkedList<BarleyValue> list, int allocated) {
        this.list = list; Storage.segment(allocated);
        this.allocated = allocated;
        this.defaultAlloc = allocated;
    }

    public Allocation(int allocated, BarleyValue... values) {
        List<BarleyValue> vals = List.of(values);
        LinkedList<BarleyValue> res = new LinkedList<>();
        for (BarleyValue val : vals) {
            res.add(val);
        }
        this.list = res;
        this.allocated = allocated;
        this.defaultAlloc = allocated;
    }

    public Allocation(int size) {
        this.list = new LinkedList<>();
        this.allocated = size;
        this.defaultAlloc = size;
    }

    public BarleyValue toList() {
        return new BarleyList(list);
    }

    public int getAllocated() {
        return allocated;
    }

    public int getDefaultAlloc() {
        return defaultAlloc;
    }

    public void segment(BarleyValue obj) {
        allocated -= StorageUtils.size(obj);
    }

    public void clear() {
        allocated = defaultAlloc;
        list.clear();
    }

    public void setList(LinkedList<BarleyValue> list) {
        this.list = list;
    }

    public void setAllocated(int allocated) {
        this.allocated = allocated;
    }

    public void setDefaultAlloc(int defaultAlloc) {
        this.defaultAlloc = defaultAlloc;
    }

    @Override
    public String toString() {
        return "#Allocation<" + hashCode() + ">";
    }

    @Override
    public BigInteger asInteger() {
        throw new BarleyException("BadArithmetic", "can't cast ALLOCATION to NUMBER");
    }

    @Override
    public BigDecimal asFloat() {
        throw new BarleyException("BadArithmetic", "can't cast ALLOCATION to NUMBER");
    }

    public LinkedList<BarleyValue> getList() {
        return list;
    }

    @Override
    public Object raw() {
        return list;
    }
}
