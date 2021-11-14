package com.barley.editor.copy;

public class Copy {
    private String _text = "";
    private boolean _isLine = false;

    private static Copy _instance = new Copy();

    public static Copy getInstance() {
        return _instance;
    }

    public String getText() {
        return _text;
    }

    public boolean isLine() {
        return _isLine;
    }

    public void setText(String text, boolean isLine) {
        _text = text;
        _isLine = isLine;
    }
}
