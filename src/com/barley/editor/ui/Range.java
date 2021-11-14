package com.barley.editor.ui;

public class Range {
    private int _start;
    private int _end;

    private Range(int start, int end) {
        _start = start;
        _end = end;
    }

    public static Range create(int start, int end) {
        return new Range(start, end);
    }

    public int getStart() {
        return _start;
    }

    public int getEnd() {
        return _end;
    }
    
    public int getLength() {
        return _end - _start;
    }
    
    public Range intersection(Range range) {
        var result = create(Math.max(_start, range._start), Math.min(_end,  range._end));
        if (result.getLength() <= 0) {
            return create(0, 0);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return "{" + _start + ", " + _end + "}";
    }
}
