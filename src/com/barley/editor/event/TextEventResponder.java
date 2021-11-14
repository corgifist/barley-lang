package com.barley.editor.event;

public class TextEventResponder implements EventResponder {
    private String[] _keyStrokes;
    private Runnable _action;

    public TextEventResponder(String string, Runnable action) {
        _keyStrokes = string.split(" ");
        _action = action;
    }

    @Override
    public Response processEvent(KeyStrokes events) {
        int processed = 0;

        for (;;) {
            var keyStroke = events.current();
            String str = _keyStrokes[processed++];
            String[] strs = str.split("-");
            boolean isCtrlModified = false;
            boolean isAltModified = false;
            if (strs.length == 2) {
                if (strs[0].equals("<CTRL>")) {
                    isCtrlModified = true;
                    str = strs[1];
                }
                if (strs[0].equals("<ALT>")) {
                    isAltModified = true;
                    str = strs[1];
                }
            }
            if (isCtrlModified != keyStroke.isCtrlDown()) {
                return Response.NO;
            }
            if (isAltModified != keyStroke.isAltDown()) {
                return Response.NO;
            }
            switch (keyStroke.getKeyType()) {
            case Character:
                if (str.equals("<SPACE>")) {
                  if (keyStroke.getCharacter() != ' ') {
                    return Response.NO;
                  }
                } else if (str.length() != 1 || keyStroke.getCharacter() != str.charAt(0)) {
                    return Response.NO;
                }
                break;
            case Escape:
                if (!str.equals("<ESC>")) {
                    return Response.NO;
                }
                break;
            case Backspace:
                if (!str.equals("<BACKSPACE>")) {
                    return Response.NO;
                }
                break;
            case Enter:
                if (!str.equals("<ENTER>")) {
                    return Response.NO;
                }
                break;
            case ArrowUp:
                if (!str.equals("<UP>")) {
                    return Response.NO;
                }
                break;
            case ArrowDown:
                if (!str.equals("<DOWN>")) {
                    return Response.NO;
                }
                break;
            case ArrowLeft:
                if (!str.equals("<LEFT>")) {
                    return Response.NO;
                }
                break;
            case ArrowRight:
                if (!str.equals("<RIGHT>")) {
                    return Response.NO;
                }
                break;
            default:
                return Response.NO;
            }

            if (processed == _keyStrokes.length) {
                events.consume(1);
                return Response.YES;
            }
            
            if (!events.hasNext()) {
                return Response.MAYBE;
            } else {
                events.consume(1);
            }
        }
    }

	@Override
	public void respond() {
      _action.run();
	}
}
