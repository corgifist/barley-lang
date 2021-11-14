package com.barley.editor;

import com.barley.editor.event.IOThread;
import com.barley.editor.terminal.TerminalContext;
import com.barley.editor.ui.Window;
import com.barley.editor.utils.LogFactory;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Path;

public class Editor {
    private static final Logger _log = LogFactory.createLog();

    private static void setupLogging() {
        try {
            File file = new File("/tmp/fisked.log");
            FileOutputStream fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            System.setErr(ps);
        } catch (Throwable e) {
        }
    }

    private static void setupWindow(Path path) {
        Window.createInstance(path);
        var window = Window.getInstance();
        window.update(true /* forced */);
    }

    private static Path checkArguments(String[] args) {
        if (args[args.length - 1].equals("-empty")) {
            try (FileWriter writer = new FileWriter("file.barley", false)) {
                writer.write("Automatically created by Barley.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return Path.of("file.barley");
        }
        if (args.length != 2) {
            System.out.println("barley: Wrong number of arguments.");
            System.out.println("Try: barley -editor <file_path> OR barley -editor -empty");
            return null;
        }

        try {
            var path = Path.of(args[1]);
            var file = path.toFile();
            if (!file.exists()) {
                try {
                    if (file.createNewFile()) {
                        return path;
                    }
                } catch (Exception e) {
                }
                System.out.println("barley: No such file: " + path.toString());
                return null;
            } else {
                return path;
            }
        } catch (Throwable e) {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            setupLogging();
            var path = checkArguments(args);
            if (path == null) {
                return;
            }
            _log.info("Barley started");
            setupWindow(path);
            var eventThread = EventThread.getInstance();
            eventThread.addOnEvent(() -> {
                Window.getInstance().update(false /* forced */);
            });
            eventThread.start();
            new IOThread(TerminalContext.getInstance().getScreen()).start();
        } catch (Exception e) {
            _log.error("Error starting: ", e);
        }
    }
}
