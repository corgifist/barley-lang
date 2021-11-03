package com.barley.utils;

import com.barley.parser.Lexer;
import com.barley.parser.Parser;
import com.barley.runtime.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Handler {

    private static String RUNTIME_VERSION = "0.1";

    public static void handle(String input, boolean isExpr, boolean time) {
        try {
            final TimeMeasurement measurement = new TimeMeasurement();
            measurement.start("Lexer time");
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            measurement.stop("Lexer time");
            measurement.start("Parse time");
            Parser parser = new Parser(tokens);
            List<AST> nodes = isExpr ? parser.parseExpr() : parser.parse();
            measurement.stop("Parse time");
            measurement.start("Execute time");
            for (AST node : nodes) {
                node.execute();
            }
            measurement.stop("Execute time");
            if (time) {
                System.out.println("======================");
                System.out.println(measurement.summary(TimeUnit.MILLISECONDS, true));
            }
        } catch (BarleyException ex) {
            System.out.printf("** exception error: %s\n", ex.getText());
            int count = CallStack.getCalls().size();
            if (count == 0) return;
            System.out.println(String.format("\nCall stack was:"));
            for (CallStack.CallInfo info : CallStack.getCalls()) {
                System.out.println("    " + count + ". " + info);
                count--;
            }
        }
    }

    public static void handle(String input, boolean isExpr) {
        Handler.handle(input, isExpr, false);
    }

    public static List<AST> parseAST(String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    public static void console() {
        System.out.printf("Barley/Java%s [barley-runtime%s] [%s] [threads-%s]\n", getVersion(), RUNTIME_VERSION, System.getProperty("os.arch"), Thread.activeCount() + ProcessTable.storage.size() + ProcessTable.receives.size());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.printf(">>> ", Thread.activeCount() + ProcessTable.storage.size() + ProcessTable.receives.size());
            try {
                Handler.handle(br.readLine(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void file(String path, boolean time) {
        try {
            Handler.handle(SourceLoader.readSource(path), false, time);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void file(String path) {
        file(path, false);
    }

    private static int getVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    public static void loadCore() {
        String[] scripts = new String[] {
                "lib/lists.barley"
        };

        for (String str : scripts) {
            try {
                Handler.handle(SourceLoader.readSource(str), false, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tests() {

        final TimeMeasurement measurement = new TimeMeasurement();

        String[] scripts = new String[] {
                "examples/bts.barley",
                "examples/lists.barley",
                "examples/prrocesses.barley",
                "examples/stack.barley",
                "examples/types.barley",
                "examples/dogs.barley"
        };

        measurement.start("Tests time");
        for(String script : scripts) {
            try {
                Handler.handle(SourceLoader.readSource(script), false, false);
                Handler.handle("test:main().", true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        measurement.stop("Tests time");
        System.out.println("======================");
        System.out.println(measurement.summary(TimeUnit.MILLISECONDS, true));
    }
}
