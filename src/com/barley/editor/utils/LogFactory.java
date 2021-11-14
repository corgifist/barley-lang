package com.barley.editor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class LogFactory {
    public static String getCallerClassName() {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        StackTraceElement element = elements[4];
        return element.getClassName();
    }

    public static Class<?> getCallerClass() {
        try {
            return Class.forName(getCallerClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Logger createLog() {
        return LoggerFactory.getLogger(Objects.requireNonNull(getCallerClass()));
    }
}
