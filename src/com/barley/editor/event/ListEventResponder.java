package com.barley.editor.event;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListEventResponder implements EventResponder {
    private EventResponder _responder;
    private List<EventResponder> _responders = new ArrayList<EventResponder>();

    public void addEventResponder(EventResponder responder) {
        _responders.add(responder);
    }

    public void addEventResponder(String pattern, Runnable runnable) {
        _responders.add(new TextEventResponder(pattern, runnable));
    }

    public void removeEventResponder(EventResponder responder) {
        _responders.remove(responder);
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        boolean maybe = false;
        EventResponder yes = null;
        for (var responder : _responders) {
            var response = responder.processEvent(new KeyStrokes(events));
            if (response == Response.MAYBE) {
                maybe = true;
            }
            if (response == Response.YES) {
                yes = responder;
            }
        }
        if (maybe) {
            return Response.MAYBE;
        } else if (yes != null) {
            _responder = yes;
            return Response.YES;
        } else {
            return Response.NO;
        }
    }

    @Override
    public void respond() {
        _responder.respond();
        _responder = null;
    }
}
