package com.barley.editor.mode;

import com.barley.editor.copy.Copy;
import com.barley.editor.ui.Cursor;
import com.barley.editor.ui.Rect;
import com.barley.editor.ui.Window;
import com.barley.editor.event.FancyJumpResponder;
import com.barley.editor.terminal.TerminalContext;
import com.barley.editor.text.AttributedString;
import com.barley.editor.text.TextLayout.Glyph;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

public class VisualMode extends Mode {
    private FancyJumpResponder _fancyJump;
    
    protected Cursor getOtherCursor() {
        return _window.getBufferContext().getBuffer().getCursors().get(1);
    }

    public VisualMode(Window window) {
        super("VISUAL", window);
        _fancyJump = new FancyJumpResponder(window.getBufferContext(), 'w');
        _rootResponder.addEventResponder(_fancyJump);
        setupBasicResponders();
        setupNavigationResponders();
    }

    protected Cursor minCursor() {
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        return cursor.getPosition() < getOtherCursor().getPosition() ? cursor : getOtherCursor();
    }

    protected Cursor maxCursor() {
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        return cursor.getPosition() >= getOtherCursor().getPosition() ? cursor : getOtherCursor();
    }

    protected void setupBasicResponders() {
        var window = _window;
        var bufferContext = window.getBufferContext();
        var buffer = bufferContext.getBuffer();
        var cursor = buffer.getCursor();
        _rootResponder.addEventResponder("<ESC>", () -> { window.switchToMode(window.getNormalMode()); });
        _rootResponder.addEventResponder("o", () -> {
            var position = cursor.getPosition();
            cursor.setPosition(getOtherCursor().getPosition());
            getOtherCursor().setPosition(position);
            bufferContext.getBufferView().adaptViewToCursor();
        });
        _rootResponder.addEventResponder("d", () -> {
            buffer.remove(minCursor().getPosition(), maxCursor().getPosition() + 1);
            window.switchToMode(window.getNormalMode());
        });
        _rootResponder.addEventResponder("c", () -> {
            buffer.remove(minCursor().getPosition(), maxCursor().getPosition() + 1);
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("y", () -> {
            var text = buffer.getSubstring(minCursor().getPosition(), maxCursor().getPosition() + 1);
            Copy.getInstance().setText(text, false /* isLine */);
            window.switchToMode(window.getNormalMode());
        });
    }

    @Override
    public void activate() {
        var other = new Cursor(_window.getBufferContext());
        other.setPosition(_window.getBufferContext().getBuffer().getCursor().getPosition());
        _window.getBufferContext().getBuffer().addCursor(other);
    }

    @Override
    public void draw(Rect rect) {
        var terminalContext = TerminalContext.getInstance();
        var graphics = terminalContext.getGraphics();
        var minCursor = minCursor();
        var maxCursor = maxCursor();
        if (maxCursor.getPosition() - minCursor.getPosition() == 0) {
            return;
        }
        int minY = minCursor.getYRelative();
        int minX = minCursor.getX();
        int maxY = maxCursor.getYRelative();
        int maxX = maxCursor.getX();
        for (int line = minY; line <= maxY; ++line) {
            int fromColumn = rect.getPoint().getX();
            int toColumn = fromColumn + rect.getSize().getWidth();
            if (line == minY) {
                fromColumn = minX;
            }
            if (line == maxY) {
                toColumn = maxX;
            }
            graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
            graphics.drawRectangle(new TerminalPosition(fromColumn, line), new TerminalSize(toColumn - fromColumn, 1), ' ');
        }
    }

    public boolean isSelected(int position) {
        var cursor = _window.getBufferContext().getBuffer().getCursor();
        var minCursor = cursor.getPosition() < getOtherCursor().getPosition() ? cursor : getOtherCursor();
        var maxCursor = cursor.getPosition() >= getOtherCursor().getPosition() ? cursor : getOtherCursor();
        return position >= minCursor.getPosition() && position <= maxCursor.getPosition();
    }
    
    @Override
    public AttributedString decorate(Glyph glyph, AttributedString character) {
        if (isSelected(glyph.getPosition())) {
            character = AttributedString.create(glyph.getCharacter(), TextColor.ANSI.BLACK, TextColor.ANSI.YELLOW);
        }
        character = _fancyJump.decorate(glyph, character);
        return character;
    }
}
