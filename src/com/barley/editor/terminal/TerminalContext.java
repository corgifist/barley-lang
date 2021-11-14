package com.barley.editor.terminal;

import java.io.IOException;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class TerminalContext {
    private static volatile TerminalContext _instance;

    public static TerminalContext getInstance() {
        TerminalContext instance = _instance;
        if (instance != null) {
            return instance;
        }
        synchronized (TerminalContext.class) {
            instance = _instance;
            if (instance != null) {
                return instance;
            }
            instance = new TerminalContext();
            _instance = instance;
        }
        return instance;
    }

    private final Screen _screen;
    private final Terminal _terminal;
    private final TextGraphics _graphics;

    public TerminalContext() {
        var factory = new DefaultTerminalFactory();
        try {
            _terminal = factory.createTerminal();
            _screen = new TerminalScreen(_terminal);
            _screen.startScreen();
        } catch (IOException e) {
            throw new RuntimeException("Can't create screen", e);
        }
        _graphics = _screen.newTextGraphics();
    }

    public TextGraphics getGraphics() {
        return _graphics;
    }

    public Screen getScreen() {
        return _screen;
    }

    public Terminal getTerminal() {
        return _terminal;
    }
}
