package com.barley.editor.event;

import java.util.Iterator;
import java.util.List;

import com.googlecode.lanterna.input.KeyStroke;

public class KeyStrokes implements Iterator<KeyStroke>, Iterable<KeyStroke> {
    private List<KeyStroke> _events;
    private int _index;
    
    public KeyStrokes(List<KeyStroke> events) {
        _events = events;
        _index = 1;
    }
    
    public KeyStrokes(KeyStrokes keyStrokes) {
        _events = keyStrokes._events;
        _index = keyStrokes._index;
    }
    
    public KeyStroke current() {
        return _events.get(_index - 1);
    }

    @Override
    public boolean hasNext() {
        return _index < _events.size();
    }

    @Override
    public KeyStroke next() {
        return _events.get(++_index);
    }
    
    public int remaining() {
        return _events.size() - _index;
    }
    
    public boolean consumed() {
        return remaining() < 0;
    }

    @Override
    public Iterator<KeyStroke> iterator() {
        var result = new KeyStrokes(this);
        result._index--;
        return result;
    }

    public void consume(int processed) {
        _index += processed;
    }
}
