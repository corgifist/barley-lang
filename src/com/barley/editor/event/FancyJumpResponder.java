package com.barley.editor.event;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.barley.editor.text.AttributedString;
import com.barley.editor.text.BufferContext;
import com.barley.editor.text.TextLayout;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyType;

public class FancyJumpResponder implements EventResponder {
    private static final Logger _log = LogFactory.createLog();
    
    private BufferContext _bufferContext;
    private List<WordResponder> _installedResponders = new ArrayList<>();
    private TextEventResponder _prefixResponder;
    private EventResponder _responder;
    
    public FancyJumpResponder(BufferContext bufferContext, char prefix) {
        _bufferContext = bufferContext;
        _prefixResponder = new TextEventResponder(Character.toString(prefix), () -> {});
    }

    private class WordResponder implements EventResponder {
        private int _position;
        private TextEventResponder _responder;
        private String _matchStringRaw;
        private int _matches = 0;
        
        public WordResponder(int position, int number, int max, char character) {
            _position = position;
            var str = new StringBuilder();
            max = (int)Math.floor(Math.log((double)max) / Math.log(52.0));
            int i;
            for (i = 0;; ++i) {
                int c = number % 52;
                str.append(" ");
                if (c < 26) {
                    str.append(Character.toString('a' + c));
                } else {
                    str.append(Character.toString('A' + c - 26));
                }
                if (number < 52) {
                    break;
                } else {
                    number /= 52;
                }
            }
            for (; i < max; ++i) {
                str.append(" a");
            }
            str.reverse();
            _matchStringRaw = str.toString().replace(" ", "");
            _log.info("Word match: " + str);
            _responder = new TextEventResponder(str.toString(), () -> {
                _log.info("Respond 3");
                _bufferContext.getBuffer().getCursor().setPosition(_position);
                _bufferContext.getBufferView().adaptViewToCursor();
                _installedResponders.clear();
            });
        }
        
        @Override
        public Response processEvent(KeyStrokes events) {
            var result = _responder.processEvent(events);
            switch (result) {
            case YES:
                break;
            case NO:
                break;
            case MAYBE:
                _matches = _matchStringRaw.length() - events.remaining();
                break;
            }
            return result;
        }
        
        @Override
        public void respond() {
            _log.info("Respond 2");
            _responder.respond();
        }

        public AttributedString decorate(TextLayout.Glyph glyph, AttributedString character) {
            if (glyph.getPosition() != _position) {
                return character;
            }
            return AttributedString.create(_matchStringRaw.substring(_matches, _matches + 1), 
                    TextColor.ANSI.RED, TextColor.ANSI.DEFAULT);
        }
    }
    
    @Override
    public Response processEvent(KeyStrokes events) {
        _installedResponders.clear();
        _log.info("Proc 0");
        var result = _prefixResponder.processEvent(events);
        if (result != Response.YES) {
            return Response.NO;
        }

        if (events.consumed()) {
            return Response.MAYBE;
        }
        
        var key = events.current();
        
        if (key.getKeyType() != KeyType.Character) {
            return Response.NO;
        }
        var responders = new ListEventResponder();
        var range = _bufferContext.getTextLayout().getGlyphRange();
        if (range.getLength() == 0) {
            return Response.NO;
        }
        var pattern = Pattern.compile("\\b" + key.getCharacter(), Pattern.MULTILINE);
        var str = _bufferContext.getBuffer().getString().substring(range.getStart(), range.getEnd());
        var matcher = pattern.matcher(str);
        var matches = new ArrayList<Integer>();
        while (matcher.find()) {
            matches.add(matcher.start());
        }
        int number = 0;
        var iter = matches.iterator();
        while (iter.hasNext()) {
            int match = iter.next();
            _log.info("Adding word responder: " + (range.getStart() + match) + ", " + number);
            var responder = new WordResponder(range.getStart() + match, number++, matches.size(), key.getCharacter());
            responders.addEventResponder(responder);
            _installedResponders.add(responder);
        }
        if (number == 0) {
            return Response.NO;
        }
        
        events.consume(1);
        
        if (events.consumed()) {
            return Response.MAYBE;
        }
        
        result = responders.processEvent(events);
        
        _responder = responders;
        
        return result;
    }

    @Override
    public void respond() {
        _responder.respond();
    }

    public AttributedString decorate(TextLayout.Glyph glyph, AttributedString character) {
        for (var responder: _installedResponders) {
            character = responder.decorate(glyph, character);
        }
        return character;
    }
}
