package com.barley.editor.event;

import java.io.IOException;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

import com.barley.editor.EventThread;

public class IOThread extends Thread {
    private Screen _screen;

    public IOThread(Screen screen) {
        _screen = screen;
    }

    @Override
    public void run() {
        while (true) {
            try {
                KeyStroke keyStroke = _screen.readInput();
                var event = new KeyStrokeEvent(keyStroke);
                EventThread.getInstance().enqueue(event);
            } catch (IOException e) {
            }
        }
    }
}
