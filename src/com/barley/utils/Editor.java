package com.barley.utils;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.internal.JansiLoader;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Editor {

    private static List<String> buffer = new ArrayList<>();
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    public static final String ANSI_RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String ANSI_BLACK = "\033[0;30m";   // BLACK
    public static final String ANSI_RED = "\033[0;31m";     // RED
    public static final String ANSI_GREEN = "\033[0;32m";   // GREEN
    public static final String ANSI_YELLOW = "\033[0;33m";  // YELLOW
    public static final String ANSI_BLUE = "\033[0;34m";    // BLUE
    public static final String ANSI_PURPLE = "\033[0;35m";  // PURPLE
    public static final String ANSI_CYAN = "\033[0;36m";    // CYAN
    public static final String ANSI_WHITE = "\033[0;37m";   // WHITE

    public static void main(String[] args) throws IOException {
        AnsiConsole.systemInstall();
        bar();
        try {
            while (true) {
                processLine();
                cls();
                bar();
                String view = String.join("", buffer);
                view = colorizeNumbers(view);
                view = fixNumbers(view);
                view = view.replaceAll("\\(", ANSI_YELLOW + "(" + ANSI_RESET);
                view = view.replaceAll("\\)", ANSI_YELLOW + ")" + ANSI_RESET);
                view = view.replaceAll("\\.", ANSI_YELLOW + "." + ANSI_RESET);
                view = view.replaceAll("-module", ANSI_GREEN + "-module" + ANSI_RESET);
                view = view.replaceAll("-opt", ANSI_GREEN + "-opt" + ANSI_RESET);
                view = view.replaceAll("-doc", ANSI_GREEN + "-doc" + ANSI_RESET);
                System.out.print(view);
            }
        } catch (Exception e) {
            e.printStackTrace();
            main(args);
        }
    }

    private static String fixNumbers(String view) {
        view = view.replaceAll("ANSI_BLUE", ANSI_BLUE);
        view = view.replaceAll("ANSI_RESET", ANSI_RESET);
        return view;
    }

    private static String colorizeNumbers(String view) {
        view = view.replaceAll("0", "ANSI_BLUE" + "0" + "ANSI_RESET");
        view = view.replaceAll("1", "ANSI_BLUE" + "1" + "ANSI_RESET");
        view = view.replaceAll("2", "ANSI_BLUE" + "2" + "ANSI_RESET");
        view = view.replaceAll("3", "ANSI_BLUE" + "3" + "ANSI_RESET");
        view = view.replaceAll("4", "ANSI_BLUE" + "4" + "ANSI_RESET");
        view = view.replaceAll("5", "ANSI_BLUE" + "5" + "ANSI_RESET");
        view = view.replaceAll("6", "ANSI_BLUE" + "6" + "ANSI_RESET");
        view = view.replaceAll("7", "ANSI_BLUE" + "7" + "ANSI_RESET");
        view = view.replaceAll("8", "ANSI_BLUE" + "8" + "ANSI_RESET");
        view = view.replaceAll("9", "ANSI_BLUE" + "9" + "ANSI_RESET");
        return view;
    }

    private static void processLine() throws IOException {
        String input = input();
        String[] parts = input.split(" ");
        if (parts[0].equals("go")) {
            System.out.printf("%s > ", parts[1]);
            String line = input();
            buffer.set(Integer.parseInt(parts[1]) - 1, line + "\n");
            return;
        } else if (parts[0].equals("save")) {
            try (FileWriter writer = new FileWriter(parts[1], false)) {
                writer.write(String.join("", buffer));
            }
            return;
        } else if (parts[0].equals("load")) {
            buffer = List.of(SourceLoader.readSource(parts[1]).split("\n"));
            List<String> newBuffer = new ArrayList<>();
            for (String line : buffer) {
                newBuffer.add(line + "\n");
            }
            buffer = newBuffer;
            return;
        } else if (parts[0].equals("clear")) {
            buffer.clear();
            return;
        } else if (parts[0].equals("exit")) {
            System.exit(0);
            return;
        }

        buffer.add(input + "\n");
    }

    private static String input() throws IOException {
        return br.readLine();
    }

    private static void bar() {
        System.out.println(ANSI_YELLOW + "=========================================================================");
        System.out.println("'go LINE' - edit LINE line; 'save FILE' save file; 'load FILE' load FILE;");
        System.out.println("'clear' clears buffer; 'exit' - exit editor");
        System.out.println("=========================================================================" + ANSI_RESET);
    }

    public final static void cls()
    {
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }
}
