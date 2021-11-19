package com.barley.editor;

import com.barley.editor.event.IOThread;
import com.barley.editor.lsp.CustomLanguageMode;
import com.barley.editor.terminal.TerminalContext;
import com.barley.editor.ui.CommandView;
import com.barley.editor.ui.Window;
import com.barley.editor.utils.LogFactory;
import com.barley.utils.SourceLoader;
import org.eclipse.lsp4j.Command;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Editor {
    public static final Map<String, CustomLanguageMode> configs = new HashMap<>();
    public static final ArrayList<String> names = new ArrayList<>();
    private static final Logger _log = LogFactory.createLog();

    private static void setupLogging() {
        try {
            File file = new File("/tmp/barley.log");
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

    private static void config(String file) throws IOException {
        String source = SourceLoader.readSource(file);
        String extension = "";
        List<String> lines = List.of(source.split("\n"));
        HashMap<String, String> rules = new HashMap<>();
        for (String rule : lines) {
            List<String> parts = List.of(rule.split(" "));
            if (parts.size() == 0) continue;
            System.out.println(parts.indexOf("->"));
            if (parts.indexOf("->") == -1) continue;
            if (parts.get(0).equals("name")) {
                consume(parts, 1, "->");
                names.add(parts.get(2));
                continue;
            }
            if (parts.get(0).equals("extension")) {
                consume(parts,1, "->");
                extension = parts.get(2);
                continue;
            }

            String regex = String.join(" ", parts.subList(0, parts.indexOf("->")));
            String color = parts.get(2);
            rules.put(regex, color);
        }
        System.out.println(extension);
        System.out.println("Loaded " + file);
        configs.put(extension, new CustomLanguageMode(names.get(names.size() - 1), rules));
    }

    private static void consume(List<String> parts, int i, String s) {
        if (!parts.get(i).equals(s))
            System.err.println("CONFIGURATION_ERROR: EXPECTED '->' AS A " + i + "TH PART");
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

        try (Stream<Path> paths = Files.walk(Paths.get("configs/"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach((a) -> {
                        try {
                            config(a.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException exception) {
            exception.printStackTrace();
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
