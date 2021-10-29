package com.barley.utils;

import com.barley.parser.Lexer;
import com.barley.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Handler {

    private static String RUNTIME_VERSION = "0.1";

    public static void handle(String input) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            List<AST> nodes = new Parser(tokens).parse();
            for (AST node : nodes) {
                System.out.println(node.execute());
            }
        } catch (BarleyException ex) {
            System.out.printf("** exception error: %s\n", ex.getText());
        }
    }

    public static void console() {
        System.out.printf("Barley/Java%s [barley-runtime%s] [%s] [threads-%s]\n", getVersion(), RUNTIME_VERSION, System.getProperty("os.arch"), Thread.activeCount());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.print(">>>");
            try {
                Handler.handle(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
}
