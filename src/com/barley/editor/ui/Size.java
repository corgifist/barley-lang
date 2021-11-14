package com.barley.editor.ui;

public class Size {
    private int _width;
    private int _height;

    private Size(int width, int height) {
        _width = width;
        _height = height;
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    public static Size create(int width, int height) {
        return new Size(width, height);
    }

    public boolean equals(Size size) {
        return _width == size._width && _height == size._height;
    }
}
