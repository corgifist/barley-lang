package com.barley.editor.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.barley.editor.ui.Point;
import com.barley.editor.ui.Range;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public class AttributedString {
    public static class AttributeSet {
        private TextColor _foregroundColour;
        private TextColor _backgroundColour;

        public AttributeSet(TextColor foregroundColour, TextColor backgroundColour) {
            _foregroundColour = foregroundColour;
            _backgroundColour = backgroundColour;
        }
    }

    public static class AttributedStringFragment {
        private String _string;
        private AttributeSet _attributes;

        public AttributedStringFragment(String string, AttributeSet attributes) {
            _string = string;
            _attributes = attributes;
        }

        public AttributeSet getAttributes() {
            return _attributes;
        }

        public String toString() {
            return _string;
        }
    }

    private List<AttributedStringFragment> _fragments = new ArrayList<>();
    private int _length = 0;

    public List<AttributedStringFragment> getFragments() {
        return _fragments;
    }

    public static AttributedString create(String string, TextColor foregroundColour, TextColor backgroundColour) {
        var str = new AttributedString();
        str.append(string, foregroundColour, backgroundColour);
        return str;
    }

    public static AttributedString create(AttributedString other) {
        var str = new AttributedString();
        str._length = other._length;
        var fragments = new ArrayList<AttributedStringFragment>();
        fragments.addAll(other._fragments);
        str._fragments = fragments;
        return str;
    }

    public void append(String string, TextColor foregroundColour, TextColor backgroundColour) {
        _fragments.add(new AttributedStringFragment(string, new AttributeSet(foregroundColour, backgroundColour)));
        _length += string.length();
    }

    public void append(AttributedString str) {
        _fragments.addAll(str._fragments);
        _length += str._length;
    }
    
    private int formatFragmentRange(Range range, Range fragmentRange, AttributeSet attrs, String fragmentStr) {
        if (range.getLength() <= 0) {
            return 0;
        }
        range = range.intersection(fragmentRange);
        if (range.getLength() <= 0) {
            return 0;
        }
        var str = fragmentStr.substring(range.getStart() - fragmentRange.getStart(), range.getEnd() - fragmentRange.getStart());
        var newFragment = new AttributedStringFragment(str, attrs);
        _fragments.add(newFragment);
        return str.length();
    }
    
    public void format(int start, int end, TextColor foregroundColour, TextColor backgroundColour) {
        var newAttr = new AttributeSet(foregroundColour, backgroundColour);
        var oldFragments = _fragments;
        int currentX = 0;
        _fragments = new ArrayList<>();
        for (var fragment: oldFragments) {
            int fragmentLength = fragment._string.length();
            var fragmentRange = Range.create(currentX, currentX + fragmentLength);
            if (currentX + fragmentLength <= start || currentX >= end) {
                _fragments.add(fragment);
            } else {
                formatFragmentRange(Range.create(currentX, start), fragmentRange, fragment._attributes, fragment._string);
                formatFragmentRange(Range.create(start, end), fragmentRange, newAttr, fragment._string);
                formatFragmentRange(Range.create(end, currentX + fragmentLength), fragmentRange, fragment._attributes, fragment._string);
            }
            currentX += fragmentLength;
        }
    }

    private static Logger _log = LogFactory.createLog();
    
    public void insert(String str, int position, TextColor foregroundColour, TextColor backgroundColour) {
        _log.info("Inserting " + str + " at " + position);
        if (position > _length || position < 0) {
            throw new IllegalArgumentException("Insert out of bounds: " + position + " length: " + _length);
        }
        var newAttr = new AttributeSet(foregroundColour, backgroundColour);
        var oldFragments = _fragments;
        int currentX = 0;
        _fragments = new ArrayList<>();
        boolean inserted = false;
        int length = 0;
        for (var fragment: oldFragments) {
            int fragmentLength = fragment._string.length();
            if (inserted ||
                    currentX + fragmentLength <= position ||
                    currentX >= position + str.length()) {
                _fragments.add(fragment);
                length += fragmentLength;
            } else {
                int splitIndex = position - currentX;
                var preString = fragment._string.substring(0, splitIndex);
                var postString = fragment._string.substring(splitIndex, fragmentLength);
                if (preString.length() > 0) {
                    _fragments.add(new AttributedStringFragment(preString, fragment._attributes));
                    length += preString.length();
                }
                _fragments.add(new AttributedStringFragment(str, newAttr));
                length += str.length();
                if (postString.length() > 0) {
                    _fragments.add(new AttributedStringFragment(postString, fragment._attributes));
                    length += postString.length();
                }
                inserted = true;
            }
            currentX += fragmentLength;
        }
        _length += str.length();
        if (length != _length) {
            throw new RuntimeException("Unexpected length: " + length + ", expected: " + _length);
        }
    }
    
    public void remove(int startPosition, int endPosition) {
        var oldFragments = _fragments;
        int currentX = _length;
        _fragments = new ArrayList<>();
        Collections.reverse(oldFragments);
        for (var fragment: oldFragments) {
            int fragmentLength = fragment._string.length();
            currentX -= fragmentLength;
            var fragmentRange = Range.create(currentX, currentX + fragmentLength);
            if (currentX + fragmentLength <= startPosition ||
                    currentX >= endPosition) {
                _fragments.add(fragment);
            } else {
                formatFragmentRange(Range.create(endPosition, currentX + fragmentLength), fragmentRange, fragment._attributes, fragment._string);
                formatFragmentRange(Range.create(currentX, startPosition), fragmentRange, fragment._attributes, fragment._string);
            }
        }
        Collections.reverse(_fragments);
        _length -= endPosition - startPosition;
    }
    
    public AttributedString getCharacter(int position) {
        int currentX = 0;
        for (var fragment: _fragments) {
            int fragmentLength = fragment._string.length();
            if (position >= currentX &&
                position + 1 <= currentX + fragmentLength) {
                return AttributedString.create(fragment._string.substring(position - currentX, position - currentX + 1), 
                        fragment._attributes._foregroundColour, fragment._attributes._backgroundColour);
            }
            currentX += fragmentLength;
        }
        throw new RuntimeException("Invalid range at " + currentX);
    }

    public void drawAt(Point point, TextGraphics graphics) {
        int currentX = 0;
        for (var fragment: _fragments) {
            graphics.setBackgroundColor(fragment.getAttributes()._backgroundColour);
            graphics.setForegroundColor(fragment.getAttributes()._foregroundColour);
            graphics.putString(point.getX() + currentX, point.getY(), fragment._string);
            currentX += fragment._string.length();
        }
    }

    public int length() {
        return _length;
    }
    
    public String toString() {
        var str = new StringBuilder();
        for (var fragment: _fragments) {
            str.append(fragment._string);
        }
        return str.toString();
    }
}
