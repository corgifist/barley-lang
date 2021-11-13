package com.barley.editor;

import jline.console.ConsoleReader;
import org.fusesource.jansi.AnsiConsole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

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

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static final ConsoleReader reader = new ConsoleReader();
    private static List<String> buffer = new ArrayList<>();

    private static final int BACKSPACE = 8;
    private static final int SHIFT = 16;
    private static final int ARROW_UP = 38;
    private static final int ARROW_DOWN = 40;
    private static final int ARROW_LEFT = 37;
    private static final int ARROW_RIGHT = 39;
    private static final int ESCAPE = 27;
    private static final int ENTER = 10;
    private static final int SAVE = 112;
    private static final int FILENAME = 113;
    private static final int SAVES = 114;
    private static final int CLEAR = 18;
    private static final int CTRL = 17;
    private static int LINE = 1;
    private static String filename = null;
    private static int POS = 0;
    private static int RAW_POS = 0;
    private static boolean autoSave = false;
    private static boolean defined = false;

    public static void main(String[] args) throws IOException, AWTException {
        AnsiConsole.systemInstall();
        bar();

        try {
            final JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
            synchronized (frame) {
                frame.addKeyListener(new KeyListener() {
                    public void keyPressed(KeyEvent e) {
                        if (filename != null && !defined) {
                            Timer t = new Timer();
                            t.schedule(new SaveTask(filename, String.join("", buffer)), 0, 60 * 1000);
                            System.out.println(ANSI_RED_BACKGROUND + "AUTO-SAVE TASK ENABLED" + ANSI_RESET);
                            defined = true;
                        }
                        frame.setVisible(false);
                        frame.dispose();
                        System.out.println(e.getKeyCode());
                        if (e.getKeyCode() == BACKSPACE) {
                            if (!buffer.isEmpty()) {
                                if (buffer.get(buffer.size() - 1).equals("\n")) {
                                    LINE--;
                                    POS = 0;
                                    RAW_POS--;
                                } else {
                                    POS--;
                                    RAW_POS--;
                                }
                                buffer.remove(RAW_POS);
                            } else {
                                LINE = 1;
                                POS = 0;
                                RAW_POS = 0;
                            }
                        } else if (e.getKeyCode() == SHIFT) {
                        } else if (e.getKeyCode() == ARROW_UP) {
                            LINE--;
                        } else if (e.getKeyCode() == ARROW_DOWN) {
                            LINE++;
                        } else if (e.getKeyCode() == ESCAPE) {
                            System.exit(0);
                        } else if (e.getKeyCode() == ENTER) {
                            buffer.add("\n");
                            LINE++;
                            POS = 0;
                        } else if (e.getKeyCode() == SAVE) {
                            save();
                        } else if (e.getKeyCode() == FILENAME) {
                            file();
                        } else if (e.getKeyCode() == SAVES) {
                            enableSaves();
                        } else if (e.getKeyCode() == CLEAR) {
                            buffer.clear();
                        } else if (e.getKeyCode() == ARROW_LEFT) {
                            RAW_POS--;
                        } else if (e.getKeyCode() == ARROW_RIGHT) {
                            RAW_POS++;
                        } else if (e.getKeyCode() == CTRL) {

                        } else {
                            POS++;
                            RAW_POS++;
                            try {
                                buffer.add(RAW_POS, String.valueOf(e.getKeyChar()));
                            } catch (IndexOutOfBoundsException ex) {
                                buffer.add(String.valueOf(e.getKeyChar()));
                            }
//                            String[] lines = String.join("", buffer).split("\n");
//                            String targetLine = lines[LINE - 1];
//                            ArrayList<String> listRepr = new ArrayList<>();
//                            for (char c : targetLine.toCharArray()) {
//                                try {
//                                    listRepr.set(POS, String.valueOf(c));
//                                } catch (IndexOutOfBoundsException ex ){
//                                    listRepr.add(String.valueOf(c));
//                                }
//                            }
//                            try {
//                                listRepr.set(POS, String.valueOf(e.getKeyChar()));
//                            } catch (IndexOutOfBoundsException ex) {
//                                listRepr.add(String.valueOf(e.getKeyChar()));
//                            }
//                            List<String> result = new ArrayList<>();
//                            for (int i = 0; i < lines.length; i++) {
//                                String current = lines[i];
//                                String[] chars = current.split("");
//                                List<String> collection = new ArrayList<>(Arrays.asList(chars));
//                                if (i + 1 == LINE) result.addAll(listRepr);
//                                else result.addAll(collection);
//                            }
//                            buffer.addAll(result);

                        }
                        cls();

                        System.out.println(buffer);
                        System.out.println(e.getKeyCode());
                        bar();
                        code();
                        System.out.println(ANSI_CYAN_BACKGROUND + "'ESC' - EXIT;" +
                                "'F1' - SAVE FILE; 'F2' - ENTER FILENAME FOR AUTO-SAVES; 'F3' - ENABLE/DISABLE AUTO-SAVES" + ANSI_RESET);
                        frame.setVisible(true);
                    }

                    public void keyReleased(KeyEvent e) {
                    }

                    public void keyTyped(KeyEvent e) {
                    }
                });
                frame.setVisible(true);
                try {
                    frame.wait();
                } catch (InterruptedException e1) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            main(args);
        }
    }

    private static void transformRight() {

    }

    private static void enableSaves() {
        cls();
        code();
        System.out.println("\n");
        System.out.println(ANSI_CYAN_BACKGROUND +
                "-------------------------------------------");
        System.out.println("| You seriously want to enable auto-saves? |");
        System.out.println("-------------------------------------------");
        System.out.println("|        [Y] - YES   |  [N] - NO           |");
        System.out.println("--------------------------------------------" + ANSI_RESET);
        String prompt = reader.readLine("choice > ");
        if (prompt.equalsIgnoreCase("N")) return;
        autoSave = true;
    }

    private static void code() {
        String currentCode = getCode();
        String[] parts = currentCode.split("\n");
        System.out.println("parts: " + List.of(parts));
        String pointed = "";
        for (int i = 0; i < parts.length; i++) {
            String str = parts[i];
            if (i + 1 == LINE) {
                String result = "";
                char[] chars = str.toCharArray();
                for (int p = 0; p < chars.length; p++) {
                    char c = chars[p];
                    if (p + 1 == POS) result += ANSI_YELLOW_BACKGROUND + c + ANSI_RESET;
                    else result += c;
                }
                pointed += result + "\n";
                continue;
            }
            pointed += str + "\n";
        }
        System.out.println(pointed);
    }

    private static String getCode() {
        String view = String.join("", buffer);
        view = colorizeNumbers(view);
        view = fixNumbers(view);
        view = string(view);
        view = operators(view);
        view = keywords(view);
        return view;
    }

    private static void file() {
        System.out.println(ANSI_CYAN_BACKGROUND + "----------------------------------");
        System.out.println("| Enter filename for auto-saving |");
        System.out.println("----------------------------------");
        filename = reader.readLine("name > ");
        System.out.println(ANSI_RESET);
    }

    private static void save() {
        cls();
        System.out.println(ANSI_CYAN_BACKGROUND +
                "-----------------------------------------");
        System.out.println("| You seriously want to save the file?  |");
        System.out.println("-----------------------------------------");
        System.out.println("|      [Y] - YES   |  [N] - NO          |");
        System.out.println("-----------------------------------------" + ANSI_RESET);
        String choice = reader.readLine(ANSI_CYAN + "Waiting for choice: ");
        if (choice.equalsIgnoreCase("n")) return;
        String file = reader.readLine(ANSI_CYAN + "Enter filename: ");
        System.out.println(ANSI_RESET);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(String.join("", buffer));
        } catch (IOException e) {
            e.printStackTrace();
        }
        cls();
        bar();
        String view = String.join("", buffer);
        view = colorizeNumbers(view);
        view = fixNumbers(view);
        view = string(view);
        view = operators(view);
        view = keywords(view);
        System.out.println(view);
        System.out.println(ANSI_CYAN_BACKGROUND +
                "'F1' - SAVE FILE; 'F2' - ENTER FILENAME FOR AUTO SAVING; 'F3' - ENABLE AUTO-SAVES"
                + ANSI_RESET);
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

//    private static void processLine(String input) throws IOException, AWTException {
//        String[] parts = input.split(" ");
//        if (parts[0].equals("go")) {
//            int index = Integer.parseInt(parts[1]) - 1;
//            System.out.print("Old was: \n" + buffer.get(index));
//            System.out.printf("%s > ", parts[1]);
//            String line = input();
//            buffer.add(index, line + "\n");
//            return;
//        } else if (parts[0].equals("insert")) {
//            int index = Integer.parseInt(parts[1]) - 1;
//            System.out.print("Old was: \n" + buffer.get(index));
//            System.out.printf("%s > ", parts[1]);
//            String line = input();
//            buffer.set(index, line + "\n");
//            return;
//        } else if (parts[0].equals("save")) {
//            try (FileWriter writer = new FileWriter(parts[1], false)) {
//                writer.write(String.join("", buffer));
//            }
//            return;
//        } else if (parts[0].equals("load")) {
//            buffer = List.of(SourceLoader.readSource(parts[1]).split("\n"));
//            List<String> newBuffer = new ArrayList<>();
//            for (String line : buffer) {
//                newBuffer.add(line + "\n");
//            }
//            buffer = newBuffer;
//            return;
//        } else if (parts[0].equals("clear")) {
//            buffer.clear();
//            return;
//        } else if (parts[0].equals("exit")) {
//            System.exit(0);
//            return;
//        } else if (parts[0].equals("eval")) {
//            if (parts.length == 2) {
//                String code = String.join("", buffer);
//                Handler.handle(code, false);
//                Handler.handle(parts[1] + ":main().", true);
//            } else {
//                Handler.entry(parts[1], parts[2]);
//            }
//            return;
//        } else if (parts[0].equals("del_range")) {
//            int start = Integer.parseInt(parts[1]);
//            int end = Integer.parseInt(parts[2]);
//            List<String> result = new ArrayList<>();
//            for (int i = 0; i < buffer.size(); i++) {
//                System.out.println(i);
//                if (betweenExclusive(i - 1, start, end)) continue;
//                result.add(buffer.get(i));
//            }
//            buffer = result;
//            return;
//        } else if (parts[0].equals("del")) {
//            int index = Integer.parseInt(parts[1]);
//            buffer.remove(index - 1);
//            return;
//        }
//        buffer.add(input + "\n");
//    }

    public static boolean betweenExclusive(int x, int min, int max) {
        return x > min && x < max;
    }

    private static String input() throws IOException {
        return reader.readLine(formatLine(buffer, buffer.size() + 1) + " - ");
    }

    private static void bar() {
        System.out.println(ANSI_CYAN_BACKGROUND + "LINE: " + LINE + "; POS = " + POS + "; 'ESC' - EXIT; 'ALT' - clear buffer; RAW_POS: " + RAW_POS + ";" + ANSI_RESET);
    }

    public final static void cls() {
        final String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Runtime.getRuntime().exec("clear");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
