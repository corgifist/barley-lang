package com.barley.editor.mode;

import com.barley.editor.copy.Copy;
import com.barley.editor.ui.Range;
import com.barley.editor.ui.Rect;
import com.barley.editor.ui.Window;
import com.barley.editor.terminal.TerminalContext;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

public class VisualLineMode extends VisualMode {
    public VisualLineMode(Window window) {
        super(window);
    }

    private Range getSelection() {
        var minLine = minCursor().getPhysicalLine();
        var maxLine = maxCursor().getPhysicalLine();
        int start = minLine.getStartPosition();
        int end = maxLine.getEndPosition(true);
        if (maxLine.getNext() == null) {
            start = Math.max(0, start - 1);
        }
        return Range.create(start, end);
    }

    private void deleteSelection() {
        var buffer = _window.getBufferContext().getBuffer();
        var selection = getSelection();
        buffer.remove(selection.getStart(), selection.getEnd());
    }

    @Override
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
            deleteSelection();
            window.switchToMode(window.getNormalMode());
        });
        _rootResponder.addEventResponder("c", () -> {
            deleteSelection();
            window.switchToMode(window.getInputMode());
        });
        _rootResponder.addEventResponder("y", () -> {
            var selection = getSelection();
            var text = buffer.getSubstring(selection.getStart(), selection.getEnd());
            Copy.getInstance().setText(text, true /* isLine */);
            window.switchToMode(window.getNormalMode());
        });
    }

    @Override
    public void draw(Rect rect) {
        var terminalContext = TerminalContext.getInstance();
        var graphics = terminalContext.getGraphics();
        var minCursor = minCursor();
        var maxCursor = maxCursor();
        int minY = minCursor.getYRelative();
        int minX = rect.getPoint().getX();
        int maxY = maxCursor.getYRelative();
        int maxX = minX + rect.getSize().getWidth();
        for (int line = minY; line <= maxY; ++line) {
            graphics.setBackgroundColor(TextColor.ANSI.YELLOW);
            graphics.drawRectangle(new TerminalPosition(minX, line), new TerminalSize(maxX - minX, 1), ' ');
        }
    }

    public boolean isSelected(int position) {
        var minPosition = minCursor().getPhysicalLine().getStartPosition();
        var maxPosition = maxCursor().getPhysicalLine().getEndPosition(false);
        if (maxCursor().getPhysicalLine().getNext() == null) {
            minPosition = Math.max(0, minPosition - 1);
        }
        return position >= minPosition && position < maxPosition;
    }
}
