package com.barley.editor.text;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.barley.editor.ui.Range;

public class TextLayout {
    public static class Glyph {
        private int _x;
        private int _y;
        private int _position;
        private String _character;

        public Glyph(int x, int y, int position, String character) {
            _x = x;
            _y = y;
            _position = position;
            _character = character;
        }

        public int getX() {
            return _x;
        }

        public int getY() {
            return _y;
        }

        public int getPosition() {
            return _position;
        }

        public String getCharacter() {
            return _character;
        }
    }

    public static class Line {
        private int _y;
        private int _startPosition;
        private boolean _isNewline;
        private Line _prev;
        private Line _next;
        private List<Glyph> _glyphs = new ArrayList<>();

        public List<Glyph> getGlyphs() {
            return _glyphs;
        }

        private Line(int y, int startPosition, Line prev, boolean isNewline) {
            _y = y;
            _startPosition = startPosition;
            _prev = prev;
            _isNewline = isNewline;
        }

        private void setNext(Line line) {
            _next = line;
        }

        public boolean isNewline() {
            return _isNewline;
        }

        public int getY() {
            return _y;
        }

        public Line getPrev() {
            return _prev;
        }

        public Line getNext() {
            return _next;
        }

        public int getIndex(int position) {
            return position - _startPosition;
        }

        public Glyph getGlyphAt(int index) {
            if (index < 0 || index >= _glyphs.size()) {
                return null;
            }
            return _glyphs.get(index);
        }

        public Glyph getLastGlyph(boolean newline) {
            if (_glyphs.size() == 0) {
                return null;
            }
            var result = _glyphs.get(_glyphs.size() - 1);
            if (!newline && result._character.equals("\n")) {
                if (_glyphs.size() == 1) {
                    return null;
                }
                result = _glyphs.get(_glyphs.size() - 2);
            }
            return result;
        }

        public String getCharacterAt(int index) {
            if (index < 0 || index >= _glyphs.size()) {
                return null;
            }
            return _glyphs.get(index).getCharacter();
        }

        public int getStartPosition() {
            return _startPosition;
        }

        public int getEndPosition(boolean newline) {
            var glyph = getLastGlyph(newline);
            if (glyph == null) {
                return getStartPosition();
            } else {
                return glyph.getPosition() + 1;
            }
        }
    }

    private ArrayList<Line> _logicalLines;
    private TreeMap<Integer, Line> _logicalLineAtPosition;
    private ArrayList<Line> _physicalLines;
    private TreeMap<Integer, Line> _physicalLineAtPosition;
    private BufferContext _bufferContext;

    public TextLayout(BufferContext bufferContext) {
        _bufferContext = bufferContext;
        calculate();
    }

    public int getIndexForPhysicalLineCharacter(int lineIndex, int characterIndex) {
        var line = _physicalLines.get((Integer)lineIndex);
        var lineStart = line.getStartPosition();
        return lineStart + characterIndex;
    }

    public Line getLogicalLineAt(int position) {
        if (position < 0) {
            position = 0;
        }
        return _logicalLineAtPosition.floorEntry(position).getValue();
    }

    public Line getPhysicalLineAt(int position) {
        if (position < 0) {
            position = 0;
        }
        return _physicalLineAtPosition.floorEntry(position).getValue();
    }

    public Line getLastPhysicalLine() {
        return _physicalLines.get(_physicalLines.size() - 1);
    }

    private static class LayoutIterator {
        Line _line;
        ArrayList<Line> _lines = new ArrayList<>();
        TreeMap<Integer, Line> _lineAtPosition = new TreeMap<>();
        int _x = 0;
        int _y = -1;
        int _position = 0;
        String _text;
        String _character;
        boolean _isNewline;
        boolean _isWrapped;
        int _width;

        LayoutIterator(String text, int width) {
            _text = text;
            _width = width;
            newLine();
        }

        void newLine() {
            ++_y;
            _x = 0;
            int position = _position;
            if (_isNewline) {
                position++;
            }
            var line  = new Line(_y, position, _line, _isNewline);
            if (_line != null) {
                _line.setNext(line);
            }
            _line = line;
            _lines.add(_y, line);
            _lineAtPosition.put(position, line);
        }

        void insertGlyph() {
            _line.getGlyphs().add(new Glyph(_x, _y, _position, _character));
        }

        void next() {
            ++_position;
        }

        void incX() {
            ++_x;
        }

        boolean hasNext() {
            if (_position < _text.length()) {
                _character = _text.substring(_position, _position + 1);
                _isNewline = _character.equals("\n");
                _isWrapped = _x == _width;
                return true;
            } else {
                return false;
            }
        }

        boolean isNewline() {
            return _isNewline;
        }

        boolean isWrapped() {
            return _isWrapped;
        }

        ArrayList<Line> getLines() {
            return _lines;
        }

        TreeMap<Integer, Line> getLineAtPosition() {
            return _lineAtPosition;
        }
    }

    private void calculateLogicalLines() {
        int width = _bufferContext.getBufferView().getBounds().getSize().getWidth();
        var string = _bufferContext.getBuffer().getString();
        var iter = new LayoutIterator(string, width);
        while (iter.hasNext()) {
            if (iter.isNewline()) {
                iter.newLine();
            } else if (iter.isWrapped()) {
                iter.newLine();
                iter.insertGlyph();
                iter.incX();
            } else {
                iter.insertGlyph();
                iter.incX();
            }
            iter.next();
        }
        _logicalLines = iter.getLines();
        _logicalLineAtPosition = iter.getLineAtPosition();
    }

    private void calculatePhysicalLines() {
        int width = _bufferContext.getBufferView().getBounds().getSize().getWidth();
        var string = _bufferContext.getBuffer().getString();
        var iter = new LayoutIterator(string, width);
        while (iter.hasNext()) {
            if (iter.isNewline()) {
                iter.insertGlyph();
                iter.newLine();
            } else {
                iter.insertGlyph();
                iter.incX();
            }
            iter.next();
        }
        _physicalLines = iter.getLines();
        _physicalLineAtPosition = iter.getLineAtPosition();
    }

    public void calculate() {
        calculateLogicalLines();
        calculatePhysicalLines();
        _bufferContext.getBufferView().setNeedsRedraw();
    }

    public Stream<Glyph> getGlyphs() {
        var bufferView = _bufferContext.getBufferView();
        var rect = bufferView.getBounds();
        var start = bufferView.getStartLine();
        var end = Math.min(start + rect.getSize().getHeight(), _logicalLines.size());
        var range = _logicalLines.subList(start, end);
        return range.stream().map((line) -> line.getGlyphs()).flatMap((list) -> list.stream());
    }
    
    public Range getGlyphRange() {
        int start = -1;
        int end = -1;
        for (var glyph: getGlyphs().collect(Collectors.toList())) {
            if (start == -1) {
                start = glyph._position;
            }
            end = glyph._position;
        }
        if (end == -1) {
            return Range.create(0, 0);
        } else {
            return Range.create(start, end + 1);
        }
    }

    public int getLogicalLineCount() {
        return _logicalLines.size();
    }

    public int getPhysicalLineCount() {
        return _physicalLines.size();
    }
}
