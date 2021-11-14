package com.barley.editor.event;

import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import com.googlecode.lanterna.input.KeyType;

public class MotionResponder implements EventResponder {
    private static final Logger _log = LogFactory.createLog();
    
    private String _motion;
    private EventResponder _prefixResponder;
    private EventResponder _motionResponder;
    
    private Responder _delegate;
    
    public static interface Responder {
        void respond(int count);
    }

    private StringBuffer _prefix = new StringBuffer();
    
    private EventResponder getInitialResponder() {
        return new EventResponder() {
            @Override
            public Response processEvent(KeyStrokes events) {
                _prefix = new StringBuffer();
                for (;;) {
                    var event = events.current();
                    if (event.getKeyType() != KeyType.Character) {
                        return Response.YES;
                    }
                    int diff = '9' - event.getCharacter();
                    if (diff >= 10 || diff < 0) {
                        return Response.YES;
                    }
                    
                    _prefix.append(Character.toString(event.getCharacter()));
                    
                    if (!events.hasNext()) {
                        events.consume(1);
                        return Response.YES;
                    } else {
                        events.consume(1);
                    }
                }
            }

            @Override
            public void respond() {
            }
        };
    }

    public MotionResponder(String motion, Responder responder) {
        _motion = motion;
        _prefixResponder = getInitialResponder();
        _motionResponder = new TextEventResponder(_motion, () -> {});
        _delegate = responder;
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        _prefixResponder.processEvent(events);
        if (events.consumed()) {
            return Response.MAYBE;
        }
        return _motionResponder.processEvent(events);
    }

    @Override
    public void respond() {
        var prefixStr = _prefix.toString();
        if (prefixStr.equals("")) {
            _delegate.respond(1);
        } else {
            _delegate.respond(Integer.parseInt(prefixStr));
        }
    }
}
