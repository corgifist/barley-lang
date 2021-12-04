package com.barley;

import com.barley.runtime.*;
import com.barley.editor.Editor;
import com.barley.utils.BarleyException;
import com.barley.utils.Handler;
import com.barley.utils.SourceLoader;
import io.github.devlinuxuser.JKey;
import org.fusesource.jansi.AnsiConsole;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

public class Main {

    public static void main(String[] args) throws IOException {
        AnsiConsole.out().print("");
        Modules.init();
        int argsLength = 0;
        if (args.length == 0) {
            Table.set("ARGS", new BarleyList());
            Handler.console();
        }

        String file = args[0];
        if (file.equals("-editor")) {
            argsLength = 2;
            LinkedList<BarleyValue> argsc = new LinkedList<>();
            for (String arg : List.of(args).subList(argsLength, args.length)) {
                argsc.add(new BarleyString(arg));
            }
            Table.set("ARGS", new BarleyList(argsc));
            Editor.main(args);
            return;
        }
        if (file.equals("-entry")) {
            argsLength = 4;
            LinkedList<BarleyValue> argsc = new LinkedList<>();
            for (String arg : List.of(args).subList(argsLength, args.length)) {
                argsc.add(new BarleyString(arg));
            }
            Table.set("ARGS", new BarleyList(argsc));
            Handler.entry(args[1], args[2]);
            return;
        }
        if (file.equals("-tests")) {
            argsLength = 1;
            LinkedList<BarleyValue> argsc = new LinkedList<>();
            for (String arg : List.of(args).subList(argsLength, args.length)) {
                argsc.add(new BarleyString(arg));
            }
            Table.set("ARGS", new BarleyList(argsc));
            Handler.tests();
            return;
        }
        argsLength = 1;
        LinkedList<BarleyValue> argsc = new LinkedList<>();
        for (String arg : List.of(args).subList(argsLength, args.length)) {
            argsc.add(new BarleyString(arg));
        }
        Table.set("ARGS", new BarleyList(argsc));
        String[] dotParts = file.split("\\.");
        if (dotParts[dotParts.length - 1].equals("app")) {
            Modules.get("dist").get("app").execute(new BarleyString(file));
        } else {
            argsLength = 1;
            try {
                Handler.handle(SourceLoader.readSource(file), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void error(String type, String text, int line, String current) {
        throw new BarleyException(type, text + "\n    at line " + line + "\n      when current line:\n            " + current);
    }
}
