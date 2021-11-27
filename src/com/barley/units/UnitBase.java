package com.barley.units;

import java.util.ArrayList;

public class UnitBase {
    private ArrayList<String> fields;

    public UnitBase(ArrayList<String> fields) {
        this.fields = fields;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "#UnitBase<" + hashCode() + ">";
    }
}
