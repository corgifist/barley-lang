package com.barley.editor.event;

public class RunnableEvent extends Event {
    private final Runnable _runnable;

    public RunnableEvent(Runnable runnable) {
        _runnable = runnable;
    }

    public void execute() {
        _runnable.run();
    }
}
