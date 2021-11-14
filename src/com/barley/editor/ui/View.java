package com.barley.editor.ui;

import java.util.ArrayList;
import java.util.List;

import com.barley.editor.event.EventResponder;
import com.barley.editor.event.KeyStrokes;
import com.barley.editor.event.Response;
import com.barley.editor.terminal.TerminalContext;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;

public class View implements Drawable, EventResponder {
    private static final Logger _log = LogFactory.createLog();
    private View _parent;
    private List<View> _subviews = new ArrayList<>();
    private Rect _bounds;
    protected TextColor _backgroundColour;
    private boolean _needsRedraw = true;
    private EventResponder _firstResponder;
    private EventResponder _lastResponder;
    private int _resizeMask = RESIZE_MASK_LEFT |
        RESIZE_MASK_RIGHT |
        RESIZE_MASK_TOP |
        RESIZE_MASK_BOTTOM;

    public static final int RESIZE_MASK_LEFT = 1;
    public static final int RESIZE_MASK_RIGHT = 2;
    public static final int RESIZE_MASK_TOP = 4;
    public static final int RESIZE_MASK_BOTTOM = 8;
    public static final int RESIZE_MASK_WIDTH = 16;
    public static final int RESIZE_MASK_HEIGHT = 32;

    public View(Rect bounds) {
        _bounds = bounds;
    }

    public void setFirstResponder(EventResponder responder) {
        _firstResponder = responder;
    }

    public Cursor getCursor() {
        var responder = _firstResponder;
        if (responder instanceof View) {
            return ((View) responder).getCursor();
        }
        return null;
    }

    @Override
    public void draw(Rect rect) {
        var terminalContext = TerminalContext.getInstance();
        var textGraphics = terminalContext.getGraphics();
        if (_backgroundColour != null) {
            textGraphics.setBackgroundColor(_backgroundColour);
            textGraphics.fillRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY()),
                                       new TerminalSize(rect.getSize().getWidth(), rect.getSize().getHeight()), ' ');
        }
    }

    public void update(Rect rect, boolean forced) {
        if (!forced && !_needsRedraw) {
            return;
        }
        _needsRedraw = false;
        draw(rect);
        for (View view: _subviews) {
            var subRect = Rect.create(rect.getPoint().getX() + view._bounds.getPoint().getX(),
                                      rect.getPoint().getY() + view._bounds.getPoint().getY(), view._bounds.getSize().getWidth(),
                                      view._bounds.getSize().getHeight());
            view.update(subRect, true /* forced */);
        }
    }

    public void setNeedsRedraw() {
        View view = this;
        while (view != null) {
            view._needsRedraw = true;
            view = view._parent;
        }
    }

    public boolean needsRedraw() {
        return _needsRedraw;
    }

    public View getParent() {
        return _parent;
    }

    public void addSubview(View view) {
        _subviews.add(view);
        view._parent = this;
    }

    public void removeFromParent() {
        if (_parent != null) {
            _parent._subviews.remove(this);
            _parent = null;
        }
    }

    public void setBackgroundColour(TextColor colour) {
        _backgroundColour = colour;
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        var firstResponder = _firstResponder;
        if (firstResponder == null) {
            return Response.NO;
        }
        var result = firstResponder.processEvent(events);
        if (result == Response.YES) {
            _lastResponder = firstResponder;
        }
        return result;
    }

    @Override
    public void respond() {
        if (_lastResponder != null) {
            _lastResponder.respond();
            _lastResponder = null;
        }
    }

    public void setResizeMask(int resizeMask) {
        _resizeMask = resizeMask;
    }

    private boolean isPinned(int mask) {
        return (_resizeMask & mask) != 0;
    }

    public void setBounds(Rect rect) {
        _bounds = rect;
    }

    public void resize(Size newParentSize) {
        Size parentSize = _parent == null ? _bounds.getSize() : _parent._bounds.getSize();
        int left = _bounds.getPoint().getX();
        int right = parentSize.getWidth() - (left + _bounds.getSize().getWidth());
        int top = _bounds.getPoint().getY();
        int bottom = parentSize.getHeight() - (top + _bounds.getSize().getHeight());
        int width = _bounds.getSize().getWidth();
        int height = _bounds.getSize().getHeight();

        int newLeft;
        int newRight;
        int newTop;
        int newBottom;

        if (isPinned(RESIZE_MASK_LEFT)) {
            newLeft = left;
        } else {
            if (!isPinned(RESIZE_MASK_RIGHT) || !isPinned(RESIZE_MASK_WIDTH)) {
                throw new RuntimeException("Layout not supported yet");
            }
            newLeft = newParentSize.getWidth() - width - right; 
        }

        if (isPinned(RESIZE_MASK_RIGHT)) {
            newRight = right;
        } else {
            if (!isPinned(RESIZE_MASK_LEFT) || !isPinned(RESIZE_MASK_WIDTH)) {
                throw new RuntimeException("Layout not supported yet");
            }
            newRight = newParentSize.getWidth() - width - left;
        }

        if (isPinned(RESIZE_MASK_TOP)) {
            newTop = top;
        } else {
            if (!isPinned(RESIZE_MASK_BOTTOM) || !isPinned(RESIZE_MASK_HEIGHT)) {
                throw new RuntimeException("Layout not supported yet");
            }
            newTop = newParentSize.getHeight() - height - bottom;
        }

        if (isPinned(RESIZE_MASK_BOTTOM)) {
            newBottom = bottom;
        } else {
            if (!isPinned(RESIZE_MASK_TOP) || !isPinned(RESIZE_MASK_HEIGHT)) {
                throw new RuntimeException("Layout not supported yet");
            }
            newBottom = newParentSize.getHeight() - height - top;
        }

        int newWidth = newParentSize.getWidth() - newLeft - newRight;
        int newHeight = newParentSize.getHeight() - newTop - newBottom;

        for (View view : _subviews) {
            view.resize(Size.create(newWidth, newHeight));
        }

        _bounds = Rect.create(newLeft, newTop, newWidth, newHeight);
        _log.info("Resizing view to " + _bounds);
    }

    public Rect getBounds() {
        return _bounds;
    }
}
