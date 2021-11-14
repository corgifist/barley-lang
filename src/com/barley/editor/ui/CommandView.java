package com.barley.editor.ui;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

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

import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

public class CommandView extends View {
    private String _message = null;
    private String _prompt = null;
    private StringBuilder _command = null;
    private ListEventResponder _responders = new ListEventResponder();
    private boolean _searchForward;
    private String _searchString;
    private static final Logger _log = LogFactory.createLog();    

    private boolean isSearch() {
        // Is this hacky? Yeah it is. Buy hey deal with it later.
        return _prompt.equals("/") || _prompt.equals("?");
    }

    public CommandView(Rect bounds) {
        super(bounds);
        _responders.addEventResponder("<ESC>", () -> {
            deactivate();
        });
        _responders.addEventResponder("<ENTER>", () -> {
            if (isSearch()) {
                runSearch(_command.toString());
            } else {
                runCommand(_command.toString().split(" "));
            }
            deactivate();
        });
        _responders.addEventResponder("<BACKSPACE>", () -> {
            if (_command.length() > 0) {
                _command.delete(_command.length() - 1, _command.length());
                CommandView.this.setNeedsRedraw();
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
                _command.append(_character);
                CommandView.this.setNeedsRedraw();
            }
        });
    }

    public void runSearch(String string) {
        var quotedString = Pattern.quote(string);
        _log.info("Searching for: " + string);
        Pattern pattern;
        try {
          pattern = Pattern.compile(quotedString);
        } catch (Throwable e) {
            _log.error("Pattern threw exception: ", e);
            return;
        }
        var cursor = Window.getInstance().getBufferContext().getBuffer().getCursor();
        _searchString = string;
        if (_prompt.equals("/")) {
            _searchForward = true;
            cursor.goNext(pattern);
        } else {
            _searchForward = false;
            cursor.goPrevious(pattern);
        }
    }

    public void searchNext() {
        if (_searchString == null) {
            return;
        }
        var pattern = Pattern.compile(_searchString);
        var cursor = Window.getInstance().getBufferContext().getBuffer().getCursor();
        if (!_searchForward) {
            cursor.goPrevious(pattern);
        } else {
            cursor.goNext(pattern);
        }
    }

    public void searchPrevious() {
        if (_searchString == null) {
            return;
        }
        var pattern = Pattern.compile(_searchString);
        var cursor = Window.getInstance().getBufferContext().getBuffer().getCursor();
        if (!_searchForward) {
            cursor.goNext(pattern);
        } else {
            cursor.goPrevious(pattern);
        }
    }

    private void runCommand(String[] args) {
        if (args.length == 0) {
            return;
        }
        String command = args[0];
        switch (command) {
            case "q":
                System.exit(0);
            case "e":
                open(args);
                break;
            case "w":
                Window.getInstance().getBufferContext().getBuffer().write();
                break;
            case "l":
                list();
                break;
        }
    }

    private void list() {
        String cwd = Path.of("").toAbsolutePath().toString();
        Path dir = Paths.get(cwd);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path file : stream) {
                setMessage(file.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open(String[] args) {
        if (args.length != 2) {
            _message = "Wrong number of parameters";
            return;
        }
        var path = Paths.get(args[1]).toAbsolutePath();
        if (!path.toFile().exists()) {
            try {
                if (path.toFile().createNewFile()) {
                    Window.getInstance().setBufferPath(path);
                    return;
                }
            } catch (Exception e) {
            }
            _message = "File does not exist";
        } else {
            Window.getInstance().setBufferPath(path);
        }
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

        graphics.setBackgroundColor(TextColor.ANSI.BLACK);
        graphics.drawRectangle(new TerminalPosition(rect.getPoint().getX(), rect.getPoint().getY()),
        new TerminalSize(rect.getSize().getWidth(), 1), ' ');

        if (_message != null) {
            var message = AttributedString.create(_message, TextColor.ANSI.DEFAULT, _backgroundColour);
            message.drawAt(rect.getPoint(), graphics);
        } else if (_command != null) {
            var message = AttributedString.create(_prompt + _command, TextColor.ANSI.DEFAULT, _backgroundColour);
            message.drawAt(rect.getPoint(), graphics);
        }
    }

    public void setMessage(String message) {
        _message = message;
        setNeedsRedraw();
    }

    public void activate(String prompt) {
        _message = null;
        _prompt = prompt;
        _command = new StringBuilder();
        var window = Window.getInstance();
        var rootView = window.getRootView();
        rootView.setFirstResponder(this);
        rootView.setNeedsRedraw();
    }

    public void deactivate() {
        _command = null;
        var window = Window.getInstance();
        var rootView = window.getRootView();
        rootView.setFirstResponder(window.getBufferContext().getBufferView());
        rootView.setNeedsRedraw();
    }
}
