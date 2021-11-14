package com.barley.editor.text;

import java.nio.file.Path;

import com.barley.editor.ui.BufferView;
import com.barley.editor.ui.Rect;

public class BufferContext {
    private Buffer _buffer;
    private BufferView _bufferView;
    private TextLayout _textLayout;

    public BufferContext(Rect rect, Path path) {
        _buffer = new Buffer(path, this);
        _bufferView = new BufferView(rect, this);
        _textLayout = new TextLayout(this);
        _buffer.open();
    }

    public Buffer getBuffer() {
        return _buffer;
    }

    public BufferView getBufferView() {
        return _bufferView;
    }

    public TextLayout getTextLayout() {
        return _textLayout;
    }
}
