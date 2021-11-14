package com.barley.editor.mode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.barley.editor.ui.Cursor;
import com.barley.editor.ui.Range;
import com.barley.editor.ui.Rect;
import com.barley.editor.ui.Window;

public class VisualBlockMode extends VisualMode {
    public VisualBlockMode(Window window) {
        super(window);
    }

    private List<Range> getSelection() {
        var selection = new ArrayList<Range>();
        var minLine = minCursor().getPhysicalLine();
        var maxLine = maxCursor().getPhysicalLine();
        int minX = Math.min(minCursor().getX(), maxCursor().getX());
        int maxX = Math.max(minCursor().getX(), maxCursor().getX()) + 1;
        var rangeX = Range.create(minX, maxX);
        var current = minLine;
        for (;;) {
            var lineRange = Range.create(0, current.getEndPosition(false) - current.getStartPosition());
            var intersectionRange = rangeX.intersection(lineRange);
            if (intersectionRange.getLength() >= 0) {
                selection.add(Range.create(intersectionRange.getStart() + current.getStartPosition(), 
                              intersectionRange.getEnd() + current.getStartPosition()));
            }
            if (current == maxLine) {
                break;
            }
            current = current.getNext();
        }
        return selection;
    }

    private void deleteSelection() {
        var buffer = _window.getBufferContext().getBuffer();
        var selection = getSelection();
        Collections.reverse(selection);
        for (var range: selection) {
            buffer.remove(range.getStart(), range.getEnd());
        }
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
        _rootResponder.addEventResponder("I", () -> {
            var selection = getSelection();
            if (selection.isEmpty()) {
                return;
            }
            window.switchToMode(window.getInputMode());
            buffer.clearCursors();
            for (var range: selection) {
                if (cursor.getPosition() >= range.getStart() && cursor.getPosition() < range.getEnd()) {
                    continue;
                }
                var newCursor = new Cursor(bufferContext);
                newCursor.setPosition(range.getStart());
                buffer.addCursor(newCursor);
            }
        });
//        _rootResponder.addEventResponder("c", () -> {
//            deleteSelection();
//            window.switchToMode(window.getInputMode());
//        });
//        _rootResponder.addEventResponder("y", () -> {
//            var selection = getSelection();
//            var text = buffer.getSubstring(selection.getStart(), selection.getEnd());
//            Copy.getInstance().setText(text, true /* isLine */);
//            window.switchToMode(window.getNormalMode());
//        });
    }

    @Override
    public void draw(Rect rect) {
    }

    public boolean isSelected(int position) {
        for (var range: getSelection()) {
            if (position >= range.getStart() && position < range.getEnd()) {
                return true;
            }
        }
        return false;
    }
}
