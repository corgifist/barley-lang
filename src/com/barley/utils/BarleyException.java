package com.barley.utils;

public class BarleyException extends RuntimeException {

    private String type;
    private String text;

    public BarleyException(String type, String text) {
        super();
        this.type = type;
        this.text = text;
    }
}
