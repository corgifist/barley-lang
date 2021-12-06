package com.barley.utils;

import com.barley.memory.Storage;
import com.barley.optimizations.*;
import com.barley.parser.Lexer;
import com.barley.parser.Parser;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.ProcessTable;
import com.barley.runtime.Table;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
            Parser parser = new Parser(tokens, input);
            List<AST> nodes = isExpr ? parser.parseExpr() : parser.parse();
            Optimization[] opts = new Optimization[]{
                    new ConstantPropagation(new ArrayList<>(nodes), new VariableGrabber()),
                    new ConstantFolding(),
                    new ExpressionSimplification(),
                    new DeadCodeElimination(),
                    new GeneratorJamming(),
            };
            measurement.stop("Parse time");
            measurement.start("Optimization time");
            if (parser.opt) {
                for (AST node : nodes) {
                    for (Optimization opt : opts) {
                        node.visit(opt);
                    }
                }
            }

            if (parser.ast) {
                System.out.println(nodes);
            }
            measurement.stop("Optimization time");
            measurement.start("Execute time");
            for (AST node : nodes) {
                node.execute();
            };
            Storage.reset();
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

    public static void entry(String input) {
        Handler.file(input);
        handle("test:main().", true);
    }

    public static void entry(String input, String module) {
        Handler.file(input, false);
        handle(module + ":main().", true);
    }

    public static List<AST> parseAST(String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, input);
        return parser.parse();
    }

    public static void console() {
        System.out.printf("Barley/Java%s [barley-runtime%s] [%s] [threads-%s]\n", getVersion(), RUNTIME_VERSION, System.getProperty("os.arch"), Thread.activeCount() + ProcessTable.storage.size() + ProcessTable.receives.size());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.printf(">>> ");
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
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }
        return Integer.parseInt(version);
    }

    public static void tests() {

        final TimeMeasurement measurement = new TimeMeasurement();

        String[] scripts = new String[]{
                "examples/bts.barley",
                "examples/lists.barley",
                "examples/prrocesses.barley",
                "examples/stack.barley",
                "examples/types.barley",
                "examples/dogs.barley",
                "examples/isolation.barley",
                "examples/queue.barley",
                "examples/measurement.barley",
                "examples/fibonacci.barley",
                "examples/bunit.barley",
                "examples/code.barley",
                "examples/expanded_remote.barley",
                "examples/unit.barley",
                "examples/strict.barley",
                "examples/error_preview.barley",
                "examples/reflection.barley",
                "examples/pointers.barley",
                "examples/segmentation.barley",
                "examples/externals.barley",
                "examples/string_interpolation.barley",
                "examples/closures.barley"
        };

        measurement.start("Tests time");
        for (String script : scripts) {
            try {
                Handler.handle(SourceLoader.readSource(script), false, true);
                Handler.handle("test:main().", true);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        measurement.stop("Tests time");
        System.out.println("======================");
        System.out.println(measurement.summary(TimeUnit.MILLISECONDS, true));
    }

    public static void calculator() {
        Handler.file("examples/calculator/lexer.barley", true);
        Handler.file("examples/calculator/parser.barley", true);
        Handler.file("examples/calculator/interpreter.barley", true);
        Handler.file("examples/calculator/program.barley", true);
        Handler.handle("test:main().", true, false);
    }

    public static void magicBall() {
        Handler.file("examples/magic_ball/m_ball_server.barley");
        Handler.file("examples/magic_ball/m_ball_client.barley");
        Handler.handle("ball_client:main().", true);
    }

    public static void amethyst() {
        Handler.file("examples/amethyst/lexer.barley");
        Handler.file("examples/amethyst/parser.barley");
        Handler.file("examples/amethyst/program.barley");
        Handler.file("examples/amethyst/interpreter.barley");
        Handler.handle("test:main().", true);
    }

    public static List<AST> parseASTExpr(String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens, input);
        return parser.parseExpr();
    }

    public static BarleyValue evalAST(String input) {
        List<AST> ast = parseASTExpr(input);
        return ast.get(0).execute();
    }
}
