package com.barley.editor.mode;

import com.barley.editor.ui.Window;
import com.barley.editor.event.EventResponder;
import com.barley.editor.event.KeyStrokes;
import com.barley.editor.event.Response;

import com.googlecode.lanterna.input.KeyType;

public class InputMode extends Mode {
    public InputMode(Window window) {
        super("INPUT", window);
        setupBasicResponders();
    }

    private void setupBasicResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        _rootResponder.addEventResponder("<ESC>", () -> {
            window.switchToMode(window.getNormalMode());
            buffer.getCursor().goLeft();
        });
        _rootResponder.addEventResponder(new EventResponder() {
            private char _character;

            @Override
            public Response processEvent(KeyStrokes events) {
                if (events.remaining() != 0) {
                    return Response.NO;
                }
                var event = events.current();
                if (event.getKeyType() == KeyType.Character) {
                    _character = event.getCharacter();
                    return Response.YES;
                }
                return Response.NO;
            }

            @Override
            public void respond() {
                buffer.insert(Character.toString(_character));
                bufferContext.getBufferView().setNeedsRedraw();
                window.getModeLineView().setNeedsRedraw();
            }
        });
        _rootResponder.addEventResponder("<BACKSPACE>", () -> { buffer.removeBefore(); });
        _rootResponder.addEventResponder("<ENTER>", () -> { buffer.insert("\n"); });
        _rootResponder.addEventResponder("<LEFT>", () -> {
            for (var c: buffer.getCursors()) {
                c.goLeft();
            }
        });
        _rootResponder.addEventResponder("<RIGHT>", () -> {
            for (var c: buffer.getCursors()) {
                c.goRight();
            }
        });
        _rootResponder.addEventResponder("<DOWN>", () -> { cursor.goDown(); });
        _rootResponder.addEventResponder("<UP>", () -> { cursor.goUp(); });
    }
}
