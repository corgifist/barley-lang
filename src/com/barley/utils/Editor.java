package com.barley.utils;

import org.fusesource.jansi.AnsiConsole;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Editor {

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
    private static List<String> buffer = new ArrayList<>();
    private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

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
                view = string(view);
                view = operators(view);
                view = keywords(view);
                List<String> lines = List.of(view.split("\n"));
                for (int i = 0; i < lines.size(); i++) {
                    System.out.printf("%s ~ %s\n", formatLine(lines, i + 1), lines.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            main(args);
        }
    }

    private static String formatLine(List<String> lines, int i) {
        int length = String.valueOf(lines.size()).length();
        String format = "0%" + length + "d";
        return String.format(format, i);
    }


    private static String operators(String view) {
        view = view.replaceAll("\\(", ANSI_YELLOW + "(" + ANSI_RESET);
        view = view.replaceAll("\\)", ANSI_YELLOW + ")" + ANSI_RESET);
        view = view.replaceAll("\\.", ANSI_YELLOW + "." + ANSI_RESET);
        view = view.replaceAll("\\=", ANSI_YELLOW + "=" + ANSI_RESET);
        view = view.replaceAll("\\>", ANSI_YELLOW + ">" + ANSI_RESET);
        view = view.replaceAll("\\<", ANSI_YELLOW + "<" + ANSI_RESET);
        view = view.replaceAll("\\-", ANSI_YELLOW + "-" + ANSI_RESET);
        view = view.replaceAll("\\|", ANSI_YELLOW + "|" + ANSI_RESET);
        view = view.replaceAll("\\?", ANSI_YELLOW + "?" + ANSI_RESET);
        view = view.replaceAll("\\:", ANSI_YELLOW + ":" + ANSI_RESET);
        return view;
    }

    private static String string(String view) {
        view = view.replaceAll("\"", ANSI_YELLOW + '"' + ANSI_RESET);
        return view;
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

    private static String keywords(String view) {
        view = view.replaceAll("guard", ANSI_CYAN + "guard" + ANSI_RESET);
        view = view.replaceAll("when", ANSI_YELLOW + "when" + ANSI_RESET);
        view = view.replaceAll("receive", ANSI_YELLOW + "receive" + ANSI_RESET);
        view = view.replaceAll("case", ANSI_YELLOW + "case" + ANSI_RESET);
        view = view.replaceAll("of", ANSI_YELLOW + "of" + ANSI_RESET);
        view = view.replaceAll("end", ANSI_YELLOW + "end" + ANSI_RESET);
        view = view.replaceAll("and", ANSI_CYAN + "and" + ANSI_RESET);
        view = view.replaceAll("or", ANSI_CYAN + "or" + ANSI_RESET);
        view = view.replaceAll("global", ANSI_YELLOW + "global" + ANSI_YELLOW);
        view = view.replaceAll("not", ANSI_CYAN + "not" + ANSI_RESET);
        view = view.replaceAll("def", ANSI_YELLOW + "def" + ANSI_RESET);
        view = view.replaceAll("module", ANSI_GREEN + "module" + ANSI_RESET);
        view = view.replaceAll("doc", ANSI_GREEN + "doc" + ANSI_RESET);
        view = view.replaceAll("opt", ANSI_GREEN + "opt" + ANSI_RESET);
        view = view.replaceAll("Rules", ANSI_PURPLE + "Rules" + ANSI_RESET);
        view = view.replaceAll("once", ANSI_PURPLE + "once" + ANSI_RESET);
        view = view.replaceAll("once_expr", ANSI_PURPLE + "once_expr" + ANSI_RESET);
        view = view.replaceAll("_expr", ANSI_PURPLE + "_expr" + ANSI_RESET);
        view = view.replaceAll("no_advance", ANSI_PURPLE + "no_advance" + ANSI_RESET);
        view = view.replaceAll("Catches", ANSI_PURPLE + "Catches" + ANSI_RESET);
        return view;
    }

    private static void processLine() throws IOException, AWTException {
        System.out.printf("%s - ", formatLine(buffer, buffer.size() + 1));
        String input = input();
        if (input.isBlank()) {
            buffer.add(input + "\n");
            return;
        }
        String[] parts = input.split(" ");
        if (parts[0].equals("go")) {
            int index = Integer.parseInt(parts[1]) - 1;
            System.out.print("Old was: \n" + buffer.get(index));
            System.out.printf("%s > ", parts[1]);
            String line = input();
            buffer.add(index, line + "\n");
            return;
        } else if (parts[0].equals("insert")) {
            int index = Integer.parseInt(parts[1]) - 1;
            System.out.print("Old was: \n" + buffer.get(index));
            System.out.printf("%s > ", parts[1]);
            String line = input();
            buffer.set(index, line + "\n");
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
        } else if (parts[0].equals("eval")) {
            if (parts.length == 2) {
                String code = String.join("", buffer);
                Handler.handle(code, false);
                Handler.handle(parts[1] + ":main().", true);
            } else {
                Handler.entry(parts[1], parts[2]);
            }
            return;
        } else if (parts[0].equals("del_range")) {
            int start = Integer.parseInt(parts[1]);
            int end = Integer.parseInt(parts[2]);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < buffer.size(); i++) {
                System.out.println(i);
                if (betweenExclusive(i - 1, start, end)) continue;
                result.add(buffer.get(i));
            }
            buffer = result;
            return;
        } else if (parts[0].equals("del")) {
            int index = Integer.parseInt(parts[1]);
            buffer.remove(index - 1);
            return;
        }
        buffer.add(input + "\n");
    }

    public static boolean betweenExclusive(int x, int min, int max)
    {
        return x>min && x<max;
    }

    private static String input() throws IOException {
        return br.readLine();
    }

    private static void bar() {
        System.out.println(ANSI_YELLOW + "=====================================================================================================");
        System.out.println("'go LINE' - edit LINE line; 'save FILE' save file; 'clear' clears buffer; 'insert INDX' inserts a txt ");
        System.out.println("'load FILE' load FILE; 'exit' - exit editor; 'eval FILE?' compiles and runs `main` (file if provided)");
        System.out.println("'del INDX' deletes line at INDX; 'del_range START END' deletes a range from START to END");
        System.out.println("=====================================================================================================" + ANSI_RESET);
    }

    public final static void cls() {
        for (int i = 0; i < 100; i++) {
            System.out.println();
        }
    }
}
