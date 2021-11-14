package com.barley.editor.ui;

public class Point {
    private int _x;
    private int _y;

    private Point(int x, int y) {
        _x = x;
        _y = y;
    }

    public int getX() {
        return _x;
    }

    public int getY() {
        return _y;
    }

    public static Point create(int x, int y) {
        return new Point(x, y);
    }

    public boolean equals(Point point) {
        return _x == point._x && _y == point._y;
    }
}
