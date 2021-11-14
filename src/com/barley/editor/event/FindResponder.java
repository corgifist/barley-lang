package com.barley.editor.event;

import java.util.regex.Pattern;

import com.barley.editor.text.BufferContext;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.input.KeyType;

public class FindResponder implements EventResponder {
    private static final Logger _log = LogFactory.createLog();

    private final MotionResponder _prefix;
    private final boolean _forward;
    private final BufferContext _context;
    private int _count;
    private char _character;

    public FindResponder(BufferContext context, String prefix, boolean forward) {
        _context = context;
        _prefix = new MotionResponder(prefix, (int count) -> { _count = count;});
        _forward = forward;
    }

    private void respond(int count, String character) {
        if (character.equals(".") ||
                character.equals("\\") ||
                character.equals("(") ||
                character.equals(")")) {
            character = "\\" + character;
        }
        var pattern = Pattern.compile(character, Pattern.MULTILINE);
        for (int i = 0; i < count; ++i) {
            if (_forward) {
                _context.getBuffer().getCursor().goNext(pattern);
            } else {
                _context.getBuffer().getCursor().goPrevious(pattern);
            }
        }
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        var result = _prefix.processEvent(events);
        if (result != Response.YES) {
            return result;
        }
        _prefix.respond();
        var event = events.current();
        if (event.getKeyType() != KeyType.Character) {
            return Response.NO;
        } else {
            _character = event.getCharacter();
            return Response.YES;
        }
    }

    @Override
    public void respond() {
        _log.info("Responding");
        respond(_count, Character.toString(_character));
    }
}
