package com.barley.editor.ui;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.barley.editor.event.EventResponder;
import com.barley.editor.event.KeyStrokes;
import com.barley.editor.event.ListEventResponder;
import com.barley.editor.event.Response;
import com.barley.editor.terminal.TerminalContext;
import com.barley.editor.text.AttributedString;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;


public class ListView extends View {
    public static abstract class ListItem {
        public abstract void onClick();
        public abstract String displayString();
    }

    private List<? extends ListItem> _list;
    private List<? extends ListItem> _filteredList;
    private String _title;
    private int _selection;
    private int _start;
    private StringBuilder _filter = new StringBuilder();
    protected ListEventResponder _responders = new ListEventResponder();

    private void filterList() {
        if (_filter.length() == 0) {
            _filteredList = _list;
            return;
        }
        var filters = _filter.toString().split(" ");
        _filteredList = _filter.length() == 0 ? _list : _list.stream().filter((item) -> {
            for (var filter: filters) {
                if (!Pattern.matches("(?i:.*" + filter + ".*)", item.displayString())) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
        setNeedsRedraw();
    }

    public ListView(Rect bounds, List<? extends ListItem> list, String title) {
        super(bounds);
        _list = list;
        _filteredList = _list;
        _title = title;
        _selection = 0;
        _responders.addEventResponder("<DOWN>", () -> {
            if (_selection >= list.size() - 1) {
                return;
            }
            ++_selection;
            ListView.this.setNeedsRedraw();
        });
        _responders.addEventResponder("<UP>", () -> {
            if (_selection <= 0) {
                return;
            }
            --_selection;
            ListView.this.setNeedsRedraw();
        });
        _responders.addEventResponder("<ESC>", () -> {
            ListView.this.getParent().setNeedsRedraw();
            Window.getInstance().hideList();
        });
        _responders.addEventResponder("<ENTER>", () -> {
            if (_selection >= _filteredList.size()) {
                return;
            }
            var item = _filteredList.get(_selection);
            ListView.this.getParent().setNeedsRedraw();
            item.onClick();
            Window.getInstance().hideList();
        });
        _responders.addEventResponder("<BACKSPACE>", () -> {
            if (_filter.length() > 0) {
                _filter.delete(_filter.length() - 1, _filter.length());
                filterList();
            }
        });
        _responders.addEventResponder(new EventResponder() {
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
                _filter.append(_character);
                filterList();
            }
        });
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        return _responders.processEvent(events);
    }

    @Override
    public void respond() {
        _responders.respond();
    }

    @Override
    public void draw(Rect rect) {
        super.draw(rect);
        var terminalContext = TerminalContext.getInstance();
        var graphics = terminalContext.getGraphics();

        graphics.setBackgroundColor(TextColor.ANSI.GREEN);
        graphics.drawRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY()), new TerminalSize(rect.getSize().getWidth(), 1), ' ');
        var title = AttributedString.create(_title, TextColor.ANSI.BLACK, TextColor.ANSI.GREEN);
        title.drawAt(rect.getPoint(), graphics);

        graphics.setBackgroundColor(TextColor.ANSI.RED);
        graphics.drawRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY() + 1),
        new TerminalSize(rect.getSize().getWidth(), 1), ' ');
        var searchText = AttributedString.create("Filter: " + _filter.toString(), TextColor.ANSI.BLACK, TextColor.ANSI.RED);
        searchText.drawAt(Point.create(rect.getPoint().getX(), rect.getPoint().getY() + 1), graphics);

        int totalHeight = rect.getSize().getHeight();
        int listHeight = totalHeight - 2;

        if (_selection >= _filteredList.size()) {
            _selection = _filteredList.size() - 1;
        }
        if (_selection < 0) {
            _selection = 0;
        }

        if (_selection >= _start + listHeight) {
            _start = _selection - listHeight + 1;
        } else if (_selection < _start) {
            _start = _selection;
        }

        for (int i = _start; i < _filteredList.size() && i - _start < listHeight; ++i) {
            var item = _filteredList.get(i);
            boolean selected = i == _selection;
            var str = AttributedString.create(item.displayString(),
                                              selected ? TextColor.ANSI.YELLOW : TextColor.ANSI.DEFAULT,
                                              selected ? TextColor.ANSI.BLACK : _backgroundColour);
            str.drawAt(Point.create(0, i - _start + rect.getPoint().getY() + 2), graphics);
        }
    }
}
