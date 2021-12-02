package com.barley.units;

import com.barley.utils.AST;

import java.util.ArrayList;
import java.util.HashMap;

public class UnitBase {

    private ArrayList<String> fields;
    private HashMap<String, AST> defaults;

    public UnitBase(ArrayList<String> fields, HashMap<String, AST> defaults) {
        this.fields = fields;
        this.defaults = defaults;
    }

    public ArrayList<String> getFields() {
        return fields;
    }

    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }

    public HashMap<String, AST> getDefaults() {
        return defaults;
    }

    public void setDefaults(HashMap<String, AST> defaults) {
        this.defaults = defaults;
    }

    @Override
    public String toString() {
        return "#UnitBase<" + hashCode() + "><" + defaults + ">";
    }
}
