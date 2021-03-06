package com.barley.runtime;

import com.barley.monty.Monty;
import com.barley.reflection.Reflection;
import com.barley.units.Unit;
import com.barley.units.UnitBase;
import com.barley.units.Units;
import com.barley.utils.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.TerminalBuilder;
import org.kohsuke.args4j.spi.ArgumentImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Modules {

    private static final HashMap<String, HashMap<String, Function>> modules = new HashMap<>();
    public static HashMap<String, String> docs = new HashMap<>();

    private static JFrame frame;
    private static CanvasPanel panel;
    private static Graphics2D graphics;
    private static BufferedImage img;

    private static BarleyValue lastKey = new BarleyNumber(-1);
    private static BarleyList mouseHover = new BarleyList(new BarleyNumber(0), new BarleyNumber(0));

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    private static void initIo() {
        HashMap<String, Function> io = new HashMap<>();
        io.put("write", args -> {
            Arguments.check(1, args.length);
            System.out.print(args[0].toString());
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("writeln", args -> {
            Arguments.check(1, args.length);
            System.out.println(args[0]);
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("format", args -> {
            Arguments.checkAtLeast(1, args.length);

            final String format = args[0].toString();
            final Object[] values = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                if (args[i] instanceof BarleyNumber) {
                    values[i - 1] = args[i].asFloat().intValue();
                } else values[i - 1] = args[i].toString();
            }
            return new BarleyString(String.format(format, values));
        });
        io.put("fwrite", args -> {
            Arguments.checkAtLeast(1, args.length);

            final String format = args[0].toString();
            final Object[] values = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                if (args[i] instanceof BarleyNumber) {
                    values[i - 1] = args[i].asFloat().intValue();
                } else values[i - 1] = args[i].toString();
            }
            System.out.print(String.format(format, values));
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("fwriteln", args -> {
            Arguments.checkAtLeast(1, args.length);

            final String format = args[0].toString();
            final Object[] values = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                if (args[i] instanceof BarleyNumber) {
                    values[i - 1] = args[i].asFloat().doubleValue();
                } else values[i - 1] = args[i].toString();
            }
            System.out.printf((format) + "%n", values);
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("readline", args -> {
            Arguments.check(0, args.length);
            return new BarleyString(new Scanner(System.in).nextLine());
        });

        modules.put("io", io);
    }

    private static void initBts() {
        HashMap<String, Function> bts = new HashMap<>();
        bts.put("new", args -> {
            Arguments.check(0, args.length);
            return new BarleyReference(new HashMap<BarleyValue, BarleyValue>());
        });
        bts.put("insert", args -> {
            Arguments.check(3, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            ((HashMap<BarleyValue, BarleyValue>) ref.getRef()).put(args[1], args[2]);
            return ref;
        });
        bts.put("tabtolist", args -> {
            Arguments.check(1, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            LinkedList<BarleyValue> result = new LinkedList<>();
            for (Map.Entry<BarleyValue, BarleyValue> entry : map.entrySet()) {
                LinkedList<BarleyValue> temporal = new LinkedList<>();
                temporal.add(entry.getKey());
                temporal.add(entry.getValue());
                result.add(new BarleyList(temporal));
            }
            return new BarleyList(result);
        });

        bts.put("tab_to_list", bts.get("tabtolist"));

        bts.put("member", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            return new BarleyAtom(AtomTable.put(String.valueOf(map.containsKey(args[1]))));
        });
        bts.put("lookup", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference ref))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            if (!(map.containsKey(args[1])))
                throw new BarleyException("BadArg", "map is empty or doesn't contains key: \n" + args[1] + "");
            return map.get(args[1]);
        });
        bts.put("remove", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            map.remove(args[1]);
            return ref;
        });
        bts.put("merge", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            if (!(args[1] instanceof BarleyReference r))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            HashMap<BarleyValue, BarleyValue> m = (HashMap<BarleyValue, BarleyValue>) r.getRef();
            HashMap<BarleyValue, BarleyValue> result = new HashMap<>(map);
            result.putAll(m);
            return new BarleyReference(result);
        });
        bts.put("copy", args -> {
            Arguments.check(1, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            HashMap<BarleyValue, BarleyValue> r = new HashMap<>();
            r.putAll(map);
            return new BarleyReference(r);
        });

        put("bts", bts);
    }

    private static String fix(String toString) {
        char[] chars = toString.toCharArray();
        ArrayList<String> chrs = new ArrayList<>();
        for (char c : chars) {
            chrs.add(String.valueOf(c));
        }
        return String.join("", chrs);
    }

    private static void initBarley() {
        HashMap<String, Function> shell = new HashMap<>();

        shell.put("expr_eval", args -> {
            List<AST> exprs = Handler.parseASTExpr(args[0].toString());
            return exprs.get(exprs.size() - 1).execute();
        });
        shell.put("ansi", args -> {
            AnsiConsole.systemInstall();
            return new BarleyAtom("ok");
        });
        shell.put("reparse", (args -> {
            Arguments.check(1, args.length);
            try {
                Handler.handle(SourceLoader.readSource(args[0].toString()), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom(AtomTable.put("ok"));
        }));
        shell.put("f", (args -> {
            Arguments.check(0, args.length);
            Table.clear();
            return new BarleyAtom(AtomTable.put("ok"));
        }));
        shell.put("b", args -> {
            Arguments.check(0, args.length);
            System.out.println(Table.variables());
            return new BarleyAtom(AtomTable.put("ok"));
        });
        shell.put("q", args -> {
            Arguments.check(0, args.length);
            System.exit(0);
            return new BarleyAtom(AtomTable.put("exit"));
        });
        shell.put("spawn", args -> {
            Arguments.checkOrOr(0, 1, args.length);
            PidValues pid = new PidValues(getRandomNumber(0, 300), getRandomNumber(0, 300), getRandomNumber(0, 300));
            BarleyPID p = new BarleyPID(pid);
            switch (args.length) {
                case 0 -> {
                    ProcessTable.put(p);
                    return p;
                }
                case 1 -> {
                    ProcessTable.put(p, args[0]);
                    return p;
                }
            }
            return new BarleyAtom(AtomTable.put("error"));
        });
        shell.put("extract_pid", args -> {
            Arguments.check(1, args.length);
            BarleyValue val = args[0];
            if (!(val instanceof BarleyPID)) throw new BarleyException("BadArgument", "expected PID as process-id");
            BarleyPID pid = (BarleyPID) val;
            return ProcessTable.get(pid);
        });

        shell.put("generator_skip", args -> {
            throw new GeneratorSkip();
        });
        shell.put("is_integer", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyNumber)));
        });
        shell.put("length", args -> {
            Arguments.check(1, args.length);
            BarleyValue arg = args[0];
            if (arg instanceof BarleyString) {
                return new BarleyNumber((arg).toString().length());
            } else if (arg instanceof BarleyList) {
                return new BarleyNumber(((BarleyList) arg).getList().size());
            } else throw new BarleyException("BadArg", "expected object that support length function");
        });
        shell.put("binary", args -> {
            LinkedList<BarleyValue> result = new LinkedList<>();
            byte[] bytes = new byte[0];
            try {
                bytes = SerializeUtils.serialize(args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (byte aByte : bytes) {
                result.add(new BarleyNumber(aByte));
            }
            return new BarleyList(result);
        });
        shell.put("from_binary", args -> {
            Arguments.check(1, args.length);
            LinkedList<BarleyValue> arr = ((BarleyList) args[0]).getList();
            ArrayList<Byte> b = new ArrayList<>();
            for (BarleyValue bt : arr) {
                b.add((byte) bt.asInteger().intValue());
            }
            Byte[] bs = b.toArray(new Byte[]{});
            byte[] binary = toPrimitives(bs);
            try {
                return SerializeUtils.deserialize(binary);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new BarleyAtom(AtomTable.put("error"));
        });
        shell.put("docs", args -> {
            Arguments.check(1, args.length);
            String module = args[0].toString();
            System.out.println(docs.get(module));
            return new BarleyAtom(AtomTable.put("ok"));
        });
        shell.put("range", args -> {
            Arguments.check(2, args.length);
            LinkedList<BarleyValue> result = new LinkedList<>();
            BarleyValue obj = args[1];
            int iteration = args[0].asFloat().intValue();
            for (int i = 0; i < iteration; i++) {
                result.add(obj);
            }
            return new BarleyList(result);
        });

        shell.put("nth", args -> {
            Arguments.check(2, args.length);
            BarleyList list = (BarleyList) args[0];
            int nth = args[1].asInteger().intValue();
            try {
                return list.getList().get(nth);
            } catch (IndexOutOfBoundsException ex) {
                return new BarleyAtom("end_of_list");
            }
        });
        shell.put("sublist", args -> {
            BarleyList list = (BarleyList) args[0];
            int from = args[1].asInteger().intValue();
            int to = args[2].asInteger().intValue();
            List<BarleyValue> subd = list.getList().subList(from, to);
            LinkedList<BarleyValue> result = new LinkedList<>(subd);
            return new BarleyList(result);
        });
        shell.put("threads_count", args -> new BarleyNumber(Thread.activeCount() + ProcessTable.storage.size() + ProcessTable.receives.size()));
        shell.put("ast_to_binary", args -> {
            Arguments.check(1, args.length);
            try {
                List<AST> parsed = Handler.parseAST(SourceLoader.readSource(args[0].toString()));
                byte[] binary = SerializeUtils.serialize((Serializable) parsed);
                LinkedList<BarleyValue> result = new LinkedList<>();
                for (byte b : binary) {
                    result.add(new BarleyNumber(b));
                }
                return new BarleyList(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom(AtomTable.put("error"));
        });
        shell.put("compile", args -> {
            Arguments.check(1, args.length);
            try {
                List<AST> parsed = Handler.parseAST(SourceLoader.readSource(args[0].toString()));
                byte[] binary = SerializeUtils.serialize((Serializable) parsed);
                try (FileWriter writer = new FileWriter(args[0].toString().split("\\.")[0] + ".ast", false)) {
                    for (byte b : binary) {
                        writer.append(String.valueOf(b)).append(" ");
                    }
                }
                return new BarleyAtom(AtomTable.put("ok"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom(AtomTable.put("error"));
        });
        shell.put("ast_from_binary", args -> {
            Arguments.check(1, args.length);
            try {
                String bytes = SourceLoader.readSource(args[0].toString());
                String[] bs = bytes.split(" ");

                ArrayList<Byte> bts = new ArrayList<>();
                for (String b : bs) {
                    bts.add(Byte.parseByte(b));
                }
                byte[] binary = toPrimitives(bts.toArray(new Byte[]{}));
                List<AST> ast = SerializeUtils.deserialize(binary);
                System.out.println("after parsing");
                for (AST node : ast) {
                    node.execute();
                }
                return new BarleyAtom(AtomTable.put("ok"));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new BarleyAtom(AtomTable.put("error"));
        });

        shell.put("atoms", args -> {
            Arguments.check(0, args.length);
            AtomTable.dump();
            return new BarleyAtom("ok");
        });

        shell.put("processes", args -> {
            Arguments.check(0, args.length);
            System.out.println(ProcessTable.receives);
            System.out.println(ProcessTable.storage);
            return new BarleyAtom("ok");
        });

        shell.put("free_process", args -> {
            Arguments.check(1, args.length);
            BarleyPID pid = (BarleyPID) args[0];
            ProcessTable.storage.remove(pid);
            ProcessTable.receives.remove(pid);
            return new BarleyAtom("ok");
        });

        shell.put("throw", args -> {
            Arguments.check(1, args.length);
            throw new BarleyException(args[0].toString(), args[0].toString());
        });

        shell.put("catch", args -> {
            Arguments.check(2, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            BarleyFunction c = (BarleyFunction) args[1];
            try {
                return fun.execute();
            } catch (BarleyException ex) {
                return c.execute(new BarleyAtom(ex.getType().toLowerCase(Locale.ROOT)));
            }
        });

        shell.put("sleep", args -> {
            Arguments.check(1, args.length);
            long time = args[0].asInteger().longValue();
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        shell.put("define", args -> {
            Arguments.check(2, args.length);
            String var = args[0].toString();
            BarleyValue value = args[1];
            Table.set(var, value);
            Table.define(var, value);
            Table.variables().put(var, value);
            return value;
        });

        shell.put("thread", args -> {
            Arguments.check(1, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            new Thread(fun::execute).start();
            return new BarleyAtom("ok");
        });

        shell.put("loop", args -> {
            while (true) {
                ((BarleyFunction) args[0]).execute();
            }
        });

        shell.put("width", args -> {
            try {
                return new BarleyNumber(TerminalBuilder.terminal().getWidth());
            } catch (IOException e) {
                return new BarleyNumber(60);
            }
        });

        shell.put("os", args -> {
            Arguments.check(1, args.length);
            String cmd = args[0].toString();
            Runtime run = Runtime.getRuntime();
            Process pr = null;
            try {
                pr = run.exec(cmd);
                pr.waitFor();
                BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
                String line = "";
                while ((line=buf.readLine())!=null) {
                    System.out.println(line);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        shell.put("height", args -> {
            try {
                return new BarleyNumber(TerminalBuilder.terminal().getHeight());
            } catch (IOException e) {
                return new BarleyNumber(120);
            }
        });

        shell.put("date", args -> new BarleyString(new Date().toString()));

        put("barley", shell);
    }

    private static void initMath() {
        HashMap<String, Function> math = new HashMap<>();

        math.put("pi", args -> {
            Arguments.check(0, args.length);
            return new BarleyNumber(Math.PI);
        });
        math.put("e", args -> {
            Arguments.check(0, args.length);
            return new BarleyNumber(Math.E);
        });
        math.put("floor", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.floor(args[0].asFloat().doubleValue()));
        });
        math.put("cos", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.cos(args[0].asFloat().doubleValue()));
        });
        math.put("tan", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.tan(args[0].asFloat().doubleValue()));
        });
        math.put("atan", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.atan(args[0].asFloat().doubleValue()));
        });
        math.put("random", args -> {
            Arguments.check(0, args.length);
            return new BarleyNumber(Math.random());
        });
        math.put("abs", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.abs(args[0].asFloat().doubleValue()));
        });
        math.put("acos", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.acos(args[0].asFloat().doubleValue()));
        });
        math.put("cbrt", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.cbrt(args[0].asFloat().doubleValue()));
        });
        math.put("ceil", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.ceil(args[0].asFloat().doubleValue()));
        });
        math.put("cosh", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.cosh(args[0].asFloat().doubleValue()));
        });
        math.put("exp", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.exp(args[0].asFloat().doubleValue()));
        });
        math.put("log", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(Math.log(args[0].asFloat().doubleValue()));
        });
        math.put("max", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(Math.max(args[0].asFloat().doubleValue(), args[1].asFloat().doubleValue()));
        });
        math.put("min", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(Math.min(args[0].asFloat().doubleValue(), args[1].asFloat().doubleValue()));
        });
        math.put("pow", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(Math.pow(args[0].asFloat().doubleValue(), args[1].asFloat().doubleValue()));
        });

        math.put("range", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(getRandomNumber(args[0].asInteger().intValue(), args[1].asInteger().intValue()));
        });

        math.put("rem", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(args[0].asInteger().intValue() % args[1].asInteger().intValue());
        });

        math.put("rsh", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(args[0].asFloat().intValue() >> args[1].asFloat().intValue());
        });

        math.put("lsh", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(args[0].asFloat().intValue() << args[1].asFloat().intValue());
        });

        math.put("shake", args -> {
            Arguments.check(2, args.length);
            return new BarleyNumber(args[0].asFloat().intValue() & args[1].asFloat().intValue());
        });



        modules.put("math", math);
    }

    private static void initString() {
        HashMap<String, Function> string = new HashMap<>();

        string.put("tab_count", args -> new BarleyNumber(new Results().getTabCount()));
        string.put("space_count", args -> new BarleyNumber(new Results().getSpaceCount()));
        string.put("length", args -> {
            Arguments.check(1, args.length);
            return new BarleyNumber(args[0].toString().length());
        });
        string.put("lower", args -> {
            Arguments.check(1, args.length);
            return new BarleyString(args[0].toString().toLowerCase(Locale.ROOT));
        });
        string.put("upper", args -> {
            Arguments.check(1, args.length);
            return new BarleyString(args[0].toString().toUpperCase(Locale.ROOT));
        });
        string.put("is_identifier", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(String.valueOf(Character.isLetterOrDigit(Character.toLowerCase(args[0].toString().charAt(0)))));
        });

        string.put("split", args -> {
            Arguments.checkOrOr(1, 2, args.length);
            LinkedList<BarleyValue> result = new LinkedList<>();
            String str = args[0].toString();
            switch (args.length) {
                case 1 -> {
                    String[] parts = str.split(" ");
                    for (String part : parts) {
                        result.add(new BarleyString(part));
                    }
                    return new BarleyList(result);
                }
                case 2 -> {
                    String[] parts_ = str.split(args[1].toString());
                    for (String part : parts_) {
                        result.add(new BarleyString(part));
                    }
                    return new BarleyList(result);
                }
                default -> throw new BarleyException("BadArg", "unexpected error was occurred");
            }
        });

        string.put("as_number", args -> {
            Arguments.check(1, args.length);
            try {
                return new BarleyNumber(Double.parseDouble(args[0].toString()));
            } catch (NumberFormatException ex) {
                return new BarleyAtom(AtomTable.put("error"));
            }
        });

        string.put("join", args -> {
            Arguments.check(2, args.length);
            LinkedList<BarleyValue> strings = ((BarleyList) args[0]).getList();
            ArrayList<String> strs = new ArrayList<>();
            for (BarleyValue str : strings) {
                strs.add(str.toString());
            }
            String delimiter = args[1].toString();
            return new BarleyString(String.join(delimiter, strs));
        });

        string.put("charat", args -> {
            Arguments.check(2, args.length);
            return new BarleyString(String.valueOf(args[0].toString().charAt(args[1].asInteger().intValue())));
        });

        string.put("replace", args -> {
            Arguments.check(3, args.length);
            return new BarleyString(args[0].toString().replaceAll(args[1].toString(), args[2].toString()));
        });

        string.put("from_bytes", args -> {
            Arguments.check(1, args.length);
            LinkedList<BarleyValue> bytes = ((BarleyList) args[0]).getList();
            byte[] bts = new byte[bytes.size()];
            for (int i = 0; i < bts.length; i++) {
                bts[i] = bytes.get(i).asInteger().byteValue();
            }
            return new BarleyString(bts);
        });

        put("string", string);
    }

    private static void initStack() {
        HashMap<String, Function> stack = new HashMap<>();

        stack.put("new", args -> {
            Arguments.check(0, args.length);
            return new BarleyReference(new Stack<BarleyValue>());
        });

        stack.put("push", args -> {
            Arguments.check(2, args.length);
            return ((Stack<BarleyValue>) ((BarleyReference) args[0]).getRef()).push(args[1]);
        });

        stack.put("is_empty", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as stack object");
            Stack<BarleyValue> st = (Stack<BarleyValue>) ((BarleyReference) s).getRef();
            return new BarleyAtom(AtomTable.put(String.valueOf(st.isEmpty())));
        });

        stack.put("pop", args -> {
            Arguments.check(1, args.length);
            Stack<BarleyValue> s = ((Stack<BarleyValue>) ((BarleyReference) args[0]).getRef());
            return s.pop();
        });

        stack.put("peek", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as stack object");
            Stack<BarleyValue> st = (Stack<BarleyValue>) ((BarleyReference) s).getRef();
            return st.peek();
        });

        stack.put("stack_to_list", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as stack object");
            Stack<BarleyValue> st = (Stack<BarleyValue>) ((BarleyReference) s).getRef();
            LinkedList<BarleyValue> result = new LinkedList<>();
            for (BarleyValue value : st) {
                result.add(value);
            }
            return new BarleyList(result);
        });

        put("stack", stack);
    }

    private static void initTypes() {
        HashMap<String, Function> types = new HashMap<>();

        types.put("is_number", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyNumber)));
        });


        types.put("is_atom", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyAtom)));
        });

        types.put("is_function", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof Function)));
        });

        types.put("is_list", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyList)));
        });

        types.put("is_pid", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyPID)));
        });

        types.put("is_string", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyString)));
        });

        types.put("ref_to_string", args -> {
            Arguments.check(1, args.length);
            return new BarleyString(((BarleyReference) args[0]).getRef().toString());
        });

        types.put("as_atom", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(args[0].toString());
        });

        put("types", types);
    }

    private static void initQueue() {
        HashMap<String, Function> queue = new HashMap<>();

        queue.put("new", args -> {
            Arguments.check(0, args.length);
            return new BarleyReference(new ConcurrentLinkedQueue<BarleyValue>());
        });

        queue.put("in", args -> {
            Arguments.check(2, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as queue object");
            ConcurrentLinkedQueue<BarleyValue> st = (ConcurrentLinkedQueue<BarleyValue>) ((BarleyReference) s).getRef();
            st.add(args[1]);
            return args[1];
        });

        queue.put("out", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as queue object");
            ConcurrentLinkedQueue<BarleyValue> st = (ConcurrentLinkedQueue<BarleyValue>) ((BarleyReference) s).getRef();
            return st.remove();
        });

        queue.put("peek", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as queue object");
            ConcurrentLinkedQueue<BarleyValue> st = (ConcurrentLinkedQueue<BarleyValue>) ((BarleyReference) s).getRef();
            return st.peek();
        });

        queue.put("q_to_list", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as queue object");
            ConcurrentLinkedQueue<BarleyValue> st = (ConcurrentLinkedQueue<BarleyValue>) ((BarleyReference) s).getRef();
            BarleyValue[] calls = st.toArray(new BarleyValue[]{});
            LinkedList<BarleyValue> result = new LinkedList<>();
            for (BarleyValue call : calls) {
                result.add(call);
            }
            return new BarleyList(result);
        });

        queue.put("is_empty", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as queue object");
            ConcurrentLinkedQueue<BarleyValue> st = (ConcurrentLinkedQueue<BarleyValue>) ((BarleyReference) s).getRef();
            return new BarleyAtom(String.valueOf(st.isEmpty()));
        });

        put("queue", queue);
    }

    private static void initMeasurement() {
        HashMap<String, Function> m = new HashMap<>();

        m.put("new", args -> {
            Arguments.check(0, args.length);
            return new BarleyReference(new TimeMeasurement());
        });

        m.put("start", args -> {
            Arguments.check(2, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as measurement object");
            TimeMeasurement st = (TimeMeasurement) ((BarleyReference) s).getRef();
            st.start(args[1].toString());
            return new BarleyAtom("ok");
        });

        m.put("stop", args -> {
            Arguments.check(2, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as measurement object");
            TimeMeasurement st = (TimeMeasurement) ((BarleyReference) s).getRef();
            st.stop(args[1].toString());
            return new BarleyAtom("ok");
        });

        m.put("pause", args -> {
            Arguments.check(2, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as measurement object");
            TimeMeasurement st = (TimeMeasurement) ((BarleyReference) s).getRef();
            st.pause(args[1].toString());
            return new BarleyAtom("ok");
        });

        m.put("summary", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected reference as measurement object");
            TimeMeasurement st = (TimeMeasurement) ((BarleyReference) s).getRef();
            System.out.println("======================");
            System.out.println(st.summary(TimeUnit.MILLISECONDS, true));
            return new BarleyAtom("ok");
        });

        put("measurement", m);
    }

    private static void initSignal() {
        HashMap<String, Function> s = new HashMap<>();
        s.put("create", args -> {
            Arguments.check(0, args.length);
            new File("tmp/").mkdir();
            try (FileWriter writer = new FileWriter("tmp/signals.txt", false)) {
                writer.append("");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        s.put("throw", args -> {
            Arguments.check(2, args.length);
            String type = args[0].toString();
            String text = args[1].toString();
            try (FileWriter writer = new FileWriter("tmp/signals.txt", false)) {
                writer.append(type + " " + text);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        s.put("on_signal", args -> {
            Arguments.check(1, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            try {
                String[] messageParts = SourceLoader.readSource("tmp/signals.txt").split(" ");
                if (List.of().isEmpty()) return new BarleyAtom("empty");
                String type = messageParts[0];
                String message = String.join(" ", List.of(messageParts).subList(1, messageParts.length));
                fun.execute(new BarleyString(type), new BarleyString(message));
                new FileWriter("tmp/signals.txt", false).append("").close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        s.put("on_named_signal", args -> {
            Arguments.check(2, args.length);
            String m = args[0].toString();
            BarleyFunction fun = (BarleyFunction) args[1];
            try {
                String[] messageParts = SourceLoader.readSource("tmp/signals.txt").split(" ");
                if (List.of(messageParts).isEmpty()) return new BarleyAtom("empty");
                String type = messageParts[0];
                if (m.equals(type)) ;
                else return new BarleyAtom("unmatch");
                String message = String.join(" ", List.of(messageParts).subList(1, messageParts.length));
                fun.execute(new BarleyString(type), new BarleyString(message));
                new FileWriter("tmp/signals.txt", false).append("").close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        put("signal", s);
    }

    private static void initCode() {
        HashMap<String, Function> code = new HashMap<>();

        code.put("load_bts", args -> {
            Arguments.check(1, args.length);
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            try {
                BarleyReference ref = (BarleyReference) args[0];
                HashMap<BarleyValue, BarleyValue> methods = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
                HashMap<String, BarleyFunction> ms = new HashMap<>();
                for (Map.Entry<BarleyValue, BarleyValue> entry : methods.entrySet()) {
                    ms.put(entry.getKey().toString(), (BarleyFunction) entry.getValue());
                }
                byte[] bytes = SerializeUtils.serialize(ms);
                LinkedList<BarleyValue> bs = new LinkedList<>();
                for (byte b : bytes) {
                    bs.add(new BarleyNumber(b));
                }
                return new BarleyList(bs);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        code.put("loaded", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(String.valueOf(modules.containsKey(args[0].toString())));
        });

        code.put("modules", args -> {
            Arguments.check(0, args.length);
            LinkedList<BarleyValue> strings = new LinkedList<>();
            for (Map.Entry<String, HashMap<String, Function>> entry : modules.entrySet()) {
                strings.add(new BarleyString(entry.getKey()));
            }
            return new BarleyList(strings);
        });

        code.put("load_binary", args -> {
            Arguments.check(2, args.length);
            try {
                String module = args[0].toString();
                LinkedList<BarleyValue> b = ((BarleyList) args[1]).getList();
                byte[] bytes = new byte[b.size()];
                for (int i = 0; i < b.size(); i++) {
                    bytes[i] = b.get(i).asInteger().byteValue();
                }
                HashMap<String, BarleyFunction> methods = SerializeUtils.deserialize(bytes);
                HashMap<String, Function> funs = new HashMap<>();
                for (Map.Entry<String, BarleyFunction> entry : methods.entrySet()) {
                    funs.put(entry.getKey(), entry.getValue());
                }
                put(module, funs);
            } catch (IOException | ClassCastException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        code.put("module_content", args -> {
            Arguments.check(1, args.length);
            HashMap<String, Function> module = get(args[0].toString());
            if (module == null) throw new BarleyException("BadArg", "module is not compiled '" + args[0] + "'");
            HashMap<BarleyValue, BarleyValue> m = new HashMap<>();
            for (Map.Entry<String, Function> entry : module.entrySet()) {
                m.put(new BarleyAtom(entry.getKey()), new BarleyFunction(entry.getValue()));
            }
            return new BarleyReference(m);
        });

        code.put("append_module", args -> {
            Arguments.check(3, args.length);
            Function fun = ((BarleyFunction) args[0]).getFunction();
            String module = args[1].toString();
            String method = args[2].toString();
            get(module).put(method, fun);
            return new BarleyAtom("ok");
        });

        code.put("delete", args -> {
            Arguments.check(1, args.length);
            modules.remove(args[0].toString());
            return new BarleyAtom("ok");
        });

        put("code", code);
    }

    private static void initBarleyUnit() {
        HashMap<String, Function> unit = new HashMap<>();

        unit.put("assert_equals", args -> {
            Arguments.check(2, args.length);
            if (args[0].equals(args[1])) return new BarleyAtom("ok");
            throw new BUnitAssertionException("values are not equals: "
                    + "1: " + args[0] + ", 2: " + args[1]);
        });
        unit.put("assert_not_equals", args -> {
            Arguments.check(2, args.length);
            if ((!args[0].equals(args[1]))) return new BarleyAtom("ok");
            throw new BUnitAssertionException("values are not equals: "
                    + "1: " + args[0] + ", 2: " + args[1]);
        });
        unit.put("assert_true", args -> {
            Arguments.check(2, args.length);
            if (args[0].toString().equals("true")) return new BarleyAtom("ok");
            throw new BUnitAssertionException("values are not equals: "
                    + "1: " + args[0] + ", 2: " + args[1]);
        });
        unit.put("assert_false", args -> {
            Arguments.check(2, args.length);
            if (args[0].toString().equals("false")) return new BarleyAtom("ok");
            throw new BUnitAssertionException("values are not equals: "
                    + "1: " + args[0] + ", 2: " + args[1]);
        });
        unit.put("run", new runTests());

        put("b_unit", unit);
    }

    private static void initFile() {
        HashMap<String, Function> file = new HashMap<>();

        file.put("read", args -> {
            Arguments.check(1, args.length);
            try {
                return new BarleyString(SourceLoader.readSource(args[0].toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        file.put("write", args -> {
            Arguments.check(2, args.length);
            try (FileWriter writer = new FileWriter(args[0].toString(), false)) {
                writer.append(args[1].toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        file.put("append", args -> {
            Arguments.check(2, args.length);
            try (FileWriter writer = new FileWriter(args[0].toString(), true)) {
                writer.append(args[1].toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        file.put("write_bytes", args -> {
            File outputFile = new File(args[0].toString());
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                LinkedList<BarleyValue> values = ((BarleyList) args[1]).getList();
                byte[] bytes = new byte[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    bytes[i] = values.get(i).asInteger().byteValue();
                }
                outputStream.write(bytes);
                return new BarleyAtom(("ok"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        put("file", file);
    }

    private static void initSocket() {
        HashMap<String, Function> socket = new HashMap<>();

        socket.put("server", args -> {
            System.out.println("Use of package 'socket' is not recommended. Wait for it's deprecation && removing and use new version!");
            Arguments.check(1, args.length);
            try {
                return new BarleyReference(new ServerSocket(args[0].asInteger().intValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        socket.put("socket", args -> {
            System.out.println("Use of package 'socket' is not recommended. Wait for it's deprecation && removing and use new version!");
            Arguments.check(2, args.length);
            String host_name = args[0].toString();
            int port = args[1].asInteger().intValue();
            try {
                return new BarleyReference(new Socket(host_name, port));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        socket.put("accept_server", args -> {
            System.out.println("Use of package 'socket' is not recommended. Wait for it's deprecation && removing and use new version!");
            Arguments.check(1, args.length);
            ServerSocket s = (ServerSocket) ((BarleyReference) args[0]).getRef();
            try {
                return new BarleyReference(s.accept());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });


        put("socket", socket);
    }

    private static void initDist() {
        HashMap<String, Function> dist = new HashMap<>();


        dist.put("entry", args -> new BarleyReference(new EntryPoint(args[0].toString(), args[1].toString())));

        dist.put("bake", args -> {
            LinkedList<BarleyValue> result = new LinkedList<>();
            String name = args[0].toString();
            result.add(new BarleyList(new BarleyString("name"), new BarleyString(name)));
            String desc = args[1].toString();
            result.add(new BarleyList(new BarleyString("desc"), new BarleyString(desc)));
            LinkedList<BarleyValue> env = new LinkedList<>();
            env.add(new BarleyString("globals"));
            for (Map.Entry<String, BarleyValue> en : Table.variables().entrySet()) {
                env.add(new BarleyList(new BarleyString(en.getKey()), en.getValue()));
            }
            result.add(new BarleyList(env));
            EntryPoint entry = (EntryPoint) ((BarleyReference) args[2]).getRef();
            List<BarleyValue> modules = List.of(args).subList(3, args.length);
            LinkedList<BarleyValue> ms = new LinkedList<>();
            ms.add(new BarleyString("modules"));
            for (BarleyValue at : modules) {
                BarleyAtom atom = (BarleyAtom) at;
                String module = at.toString();
                HashMap<String, Function> methods = get(module);
                LinkedList<BarleyValue> method = new LinkedList<>();
                method.add(atom);
                for (Map.Entry<String, Function> ent : methods.entrySet()) {
                    method.add(new BarleyList(new BarleyString(ent.getKey()), new BarleyFunction(ent.getValue())));
                }
                if (docs.containsKey(module)) {
                    method.add(new BarleyList(new BarleyString("doc"), new BarleyString(docs.get(module))));
                } else method.add(new BarleyList(new BarleyString("doc"), new BarleyString("no docs provided")));
                ms.add(new BarleyList(method));
            }
            result.add(new BarleyList(ms));
            result.add(new BarleyList(new BarleyString("entry_point"), new BarleyReference(entry)));
            return new BarleyList(result);
        });

        dist.put("write", args -> {
            Arguments.check(2, args.length);
            try (FileWriter writer = new FileWriter(args[0].toString() + ".app", false)) {
                byte[] bytes = SerializeUtils.serialize(args[1]);
                StringBuilder result = new StringBuilder();
                for (byte b : bytes) {
                    result.append(b).append(" ");
                }
                writer.write(result.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        dist.put("raw_app", args -> {
            Arguments.check(1, args.length);
            BarleyList root = ((BarleyList) args[0]);
            String name = ((BarleyList) root.getList().get(0))
                    .getList().get(1).toString();
            String desc = ((BarleyList) root.getList().get(1))
                    .getList().get(1).toString();
            Table.define("APP_NAME", new BarleyString(name));
            Table.define("APP_DESC", new BarleyString(desc));
            LinkedList<BarleyValue> globals = ((BarleyList) root.getList().get(2)).getList();
            for (BarleyValue global : globals) {
                if (global.toString().equals("globals")) continue;
                BarleyList g = (BarleyList) global;
                String n = g.getList().get(0).toString();
                BarleyValue val = g.getList().get(1);
                Table.define(n, val);
            }
            LinkedList<BarleyValue> modules = ((BarleyList) root.getList().get(3)).getList();
            for (BarleyValue module : modules) {
                if (module.toString().equals("modules")) continue;
                HashMap<String, Function> map = new HashMap<>();
                String m_name = ((BarleyList) module).getList().get(0).toString();
                List<BarleyValue> m = ((BarleyList) module).getList().subList(1, ((BarleyList) module).getList().size());
                for (BarleyValue method : m) {
                    if (((BarleyList) method).getList().get(0).toString().equals("doc")) {
                        docs.put(m_name, ((BarleyList) method).getList().get(1).toString());
                        continue;
                    }
                    String f_name = ((BarleyList) method).getList().get(0).toString();
                    Function f = ((BarleyFunction) ((BarleyList) method).getList().get(1)).getFunction();
                    map.put(f_name, f);
                }
                put(m_name, map);
            }
            EntryPoint point = (EntryPoint) ((BarleyReference) (((BarleyList) root.getList().get(4)).getList().get(1))).getRef();
            Function main = get(point.getName()).get(point.getMethod());
            return main.execute();
        });

        dist.put("app", args -> {
            Arguments.check(1, args.length);
            Function fun = dist.get("raw_app");
            try {
                String[] bts = SourceLoader.readSource(args[0].toString()).split(" ");
                List<Byte> bs = new ArrayList<>();
                for (String str : bts) {
                    bs.add(Byte.parseByte(str));
                }
                Byte[] bt = bs.toArray(new Byte[]{});
                byte[] bytes = toPrimitives(bt);
                BarleyValue app = SerializeUtils.deserialize(bytes);
                return fun.execute(app);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        put("dist", dist);
    }

    private static String current() {
        return modules.toString();
    }

    private static void initAmethyst() {
        HashMap<String, Function> am = new HashMap<>();

        am.put("lexer", args -> {
            Arguments.checkOrOr(1, 2, args.length);
            try {
                String lexerFile = SourceLoader.readSource(args[0].toString());
                String result = "";
                String[] lines = lexerFile.split("\\n");
                Map<String, String> macros = new HashMap<>();
                int cutAfterRules = 1;
                for (String line : lines) {
                    if (line.isEmpty() || line.isBlank()) continue;
                    String[] parts = line.split(" ");
                    cutAfterRules++;
                    if (parts.length == 1) break;
                    String id = parts[0];
                    if (!parts[1].equals("=")) {
                        System.err.println("Lexer Warning: Expected '=' after macros name, got '" + parts[1] + "'");
                        break;
                    }
                    String rep = String.join(" ", List.of(parts).subList(2, parts.length));
                    result += "global " + id + "\n = " + rep + "\n.\n";
                }
                List<String> rules = List.of(lines).subList(cutAfterRules, lines.length);
                int cutToCatches = 0;
                for (String line : rules) {
                    if (line.length() == 7) break;
                    cutToCatches++;
                }
                String[] sCatch = lexerFile.split("Catches");
                result += sCatch[sCatch.length - 1];
                result += "\n";
                // Rules transformation
                result += "\n";
                result += "global Pos = 0.\n";
                result += "global Line = 1.\n";
                result += "global EOFToken = [eof, -1, \"\"].\n\n";
                result += "peek(Parts, RelativePos) ->\n" +
                        "    FinalPosition = RelativePos + Pos,\n" +
                        "    lists:nth(Parts, FinalPosition).\n" +
                        "\n" +
                        "next(Parts) ->\n" +
                        "    barley:define(\"Pos\", Pos + 1),\n" +
                        "    peek(Parts, 0).\n";
                result += "illegal_character(S, L) -> barley:throw(\"illegal char '\" + S + \"' at line \" + Line).\n" +
                        "\n" +
                        "lex(String) -> lex(String, 1).\n" +
                        "\n" +
                        "lex(String, Line) ->\n" +
                        "    Pos = 0,\n" +
                        "    Line = 1,\n" +
                        "    process_parts(string:split(String, \"\")).\n";
                StringBuilder process = new StringBuilder("\n");
                for (String rule : rules) {
                    if (rule.isEmpty() || rule.isBlank()) continue;
                    String[] parts = rule.split(" ");
                    StringBuilder buffer = new StringBuilder();
                    if (parts[0].equals("once")) {
                        String expr = macros.containsKey(parts[1]) ? rule.substring(5, rule.indexOf("->")).replaceAll(parts[1], macros.get(parts[1])) : String.format("%s", rule.substring(5, rule.indexOf("->")));
                        String res = String.join(" ", List.of(parts).subList(List.of(parts).indexOf("->") + 1, parts.length));
                        buffer.append("process_part(Parts, Symbol) when Symbol == \n ")
                                .append(expr)
                                .append("\n -> \n")
                                .append("  next(Parts),\n  ")
                                .append(res)
                                .append("\n.\n");
                        process.append(buffer);
                        continue;
                    }
                    if (parts[0].equals("no_advance")) {
                        String expr = macros.containsKey(parts[1]) ? rule.substring(5, rule.indexOf("->")).replaceAll(parts[1], macros.get(parts[1])) : String.format("%s", rule.substring(5, rule.indexOf("->")));
                        String res = String.join(" ", List.of(parts).subList(List.of(parts).indexOf("->") + 1, parts.length));
                        buffer.append("process_part(Parts, Symbol) when Symbol == \n")
                                .append(expr)
                                .append("\n -> \n")
                                .append(res)
                                .append("\n.\n");
                        process.append(buffer);
                        continue;
                    }
                    if (parts[0].equals("no_advance_expr")) {
                        String expr = String.format("%s", rule.substring(16, rule.indexOf("->")));
                        String res = String.join(" ", List.of(parts).subList(List.of(parts).indexOf("->") + 1, parts.length));
                        buffer.append("process_part(Parts, Symbol) when \n")
                                .append(expr)
                                .append("\n -> \n  ")
                                .append(res)
                                .append("\n.\n");
                        process.append(buffer);
                        continue;
                    }
                    if (parts[0].equals("once_expr")) {
                        String expr = String.format("%s", rule.substring(10, rule.indexOf("->")));
                        String res = String.join(" ", List.of(parts).subList(List.of(parts).indexOf("->") + 1, parts.length));
                        process.append("process_part(Parts, Symbol) when ").append(expr).append(" -> \n").append("  next(Parts), \n  ").append(res).append("\n");
                        process.append(".\n");
                        continue;
                    }
                    if (parts[0].equals("skip")) {
                        List<String> ps = List.of(parts).subList(2, parts.length);
                        String expr = String.join(" ", ps);

                        process.append("process_part(Parts, Symbol)\n when Symbol == \n  ")
                                .append(expr)
                                .append("\n -> \n")
                                .append("  next(Parts), \n  ")
                                .append("[skip, Line, \"\"]")
                                .append(".\n");
                    }

                    if (parts[0].equals("line_increase")) {
                        List<String> ps = List.of(parts).subList(2, parts.length);
                        String expr = String.join(" ", ps);

                        process.append("process_part(Parts, Symbol)\n when Symbol == \n  ")
                                .append(expr)
                                .append("\n -> \n")
                                .append("Line = Line + 1, Pos = Pos + 1, [skip, Line + 1, \"\"].\n");
                    }

                    if (parts[0].equals("anyway")) {
                        String r = String.join(" ", List.of(parts).subList(2, parts.length));
                        process.append("process_part(Parts, Symbol) ->\n    ")
                                .append(r)
                                .append("\n")
                                .append(".");
                    }
                }
                result += process + "\nprocess_part(Parts, Symbol) when Symbol == end_of_list -> EOFToken.\n";
                result += "\n";
                result += "process_parts(Parts) ->\n" +
                        "    Result = lists:reduce(def (X, Acc) -> First = peek(Parts, 0), Acc + [process_part(Parts, First)]. end, Parts, []),\n" +
                        "    WithoutEOF = lists:filter(def (X) -> (not (lists:nth(X, 0) == eof)). end, Result),\n" +
                        "    WithoutEOF = lists:filter(def (X) -> (not (lists:nth(X, 0) == skip)). end, WithoutEOF),\n" +
                        "    WithoutEOF = WithoutEOF + [EOFToken].";
                result = "-module(" + (args.length == 1 ? args[0].toString().split("\\.")[0] : args[1].toString()) + ").\n\n" + result;
                try (FileWriter writer = new FileWriter(args[0].toString().split("\\.")[0] + ".barley")) {
                    writer.write(result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });
        am.put("parser", args -> {
            try {
                String parserFile = SourceLoader.readSource(args[0].toString());
                String root = parserFile.split("\n")[0].split(" ")[1];
                String result = String.join("\n", List.of(parserFile.split("\n")).subList(1, parserFile.split("\n").length));
                String parser = "";
                parser += "-module(" + (args.length == 1 ? args[0].toString().split("\\.")[0] : args[1].toString()) + ").\n\n";
                parser += "global Pos = 0.\n" +
                        "global Size = 0.\n" +
                        "global Tokens = [].\n" +
                        "global Result = [].\n" +
                        "\n" +
                        "\n" +
                        "type(Tok) -> lists:nth(Tok, 0).\n" +
                        "text(Tok) -> lists:nth(Tok, 2).\n" +
                        "\n" +
                        "consume_in_bounds(P) when P < Size -> P.\n" +
                        "consume_in_bounds(P) -> Size - 1.\n" +
                        "\n" +
                        "consume_type(Token, Type) -> type(Token) == Type.\n" +
                        "\n" +
                        "get(RelativePos) ->\n" +
                        "    FinalPosition = Pos + RelativePos,\n" +
                        "    P = consume_in_bounds(FinalPosition),\n" +
                        "    lists:nth(Tokens, P).\n" +
                        "\n" +
                        "eval_match(C, T) when type(C) == T -> Pos = Pos + 1, true.\n" +
                        "\n" +
                        "eval_match(C, T) -> false.\n" +
                        "\n" +
                        "match(TokenType) ->\n" +
                        "    C = get(0),\n" +
                        "    eval_match(C, TokenType).\n\n";
                parser += "expr() -> " + root + "().\n\n";
                parser += result + "\n";
                parser += "make_parse() when match(eof) -> Result.\n" +
                        "make_parse() -> Expr = [expr()],\n" +
                        "                Result = Result + Expr,\n" +
                        "                make_parse().\n" +
                        "\n" +
                        "parse(Toks) ->\n" +
                        "    Pos = 0,\n" +
                        "    Tokens = Toks,\n" +
                        "    Size = barley:length(Toks),\n" +
                        "    Result = [],\n" +
                        "    make_parse().\n";
                try (FileWriter writer = new FileWriter(args[0].toString().split("\\.")[0] + ".barley")) {
                    writer.write(parser);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("ok");
        });

        put("amethyst", am);
    }

    private static void dumpMacros(String key, String value) {
        System.out.printf("Key: %s, Value: %s\n", key, value);
    }

    public static void init() {
        initBarley();
        initIo();
        initBts();
        initMath();
        initString();
        initStack();
        initTypes();
        initQueue();
        initMeasurement();
        initSignal();
        initCode();
        initBarleyUnit();
        initFile();
        // initSocket(); Deprecation
        initDist();
        initLists();
        initAmethyst();
        initInterface();
        initAnsi();
        initUnit();
        initReflection();
        initBase();
        initHttp();
        initMonty();
    }

    public static void initMonty() {
        HashMap<String, Function> monty = new HashMap<>();

        monty.put("dispose_on_close", args -> new BarleyNumber(JFrame.DISPOSE_ON_CLOSE));
        monty.put("do_nothing_on_close", args -> new BarleyNumber(JFrame.DO_NOTHING_ON_CLOSE));
        monty.put("exit_on_close", args -> new BarleyNumber(JFrame.EXIT_ON_CLOSE));
        monty.put("hide_on_close", args -> new BarleyNumber(JFrame.HIDE_ON_CLOSE));

        // Swing constants

        monty.put("swing_bottom", args -> new BarleyNumber(SwingConstants.BOTTOM));
        monty.put("swing_center", args -> new BarleyNumber(SwingConstants.CENTER));
        monty.put("swing_east", args -> new BarleyNumber(SwingConstants.EAST));
        monty.put("swing_horiz", args -> new BarleyNumber(SwingConstants.HORIZONTAL));
        monty.put("swing_leading", args -> new BarleyNumber(SwingConstants.LEADING));
        monty.put("swing_left", args -> new BarleyNumber(SwingConstants.LEFT));
        monty.put("swing_next", args -> new BarleyNumber(SwingConstants.NEXT));
        monty.put("swing_north", args -> new BarleyNumber(SwingConstants.NORTH));
        monty.put("swing_north_east", args -> new BarleyNumber(SwingConstants.NORTH_EAST));
        monty.put("swing_north_west", args -> new BarleyNumber(SwingConstants.NORTH_WEST));
        monty.put("swing_previous", args -> new BarleyNumber(SwingConstants.PREVIOUS));
        monty.put("swing_right", args -> new BarleyNumber(SwingConstants.RIGHT));
        monty.put("swing_south", args -> new BarleyNumber(SwingConstants.SOUTH));
        monty.put("swing_south_east", args -> new BarleyNumber(SwingConstants.SOUTH_EAST));
        monty.put("swing_south_west", args -> new BarleyNumber(SwingConstants.SOUTH_WEST));
        monty.put("swing_top", args -> new BarleyNumber(SwingConstants.TOP));
        monty.put("swing_trailing", args -> new BarleyNumber(SwingConstants.TRAILING));
        monty.put("swing_vertical", args -> new BarleyNumber(SwingConstants.VERTICAL));
        monty.put("swing_west", args -> new BarleyNumber(SwingConstants.WEST));

        // Layout managers constants

        monty.put("AFTER_LAST_LINE".toLowerCase(), args -> new BarleyString(BorderLayout.AFTER_LAST_LINE));
        monty.put("AFTER_LINE_ENDS".toLowerCase(), args -> new BarleyString(BorderLayout.AFTER_LINE_ENDS));
        monty.put("BEFORE_FIRST_LINE".toLowerCase(), args -> new BarleyString(BorderLayout.BEFORE_FIRST_LINE));
        monty.put("BEFORE_LINE_BEGINS".toLowerCase(), args -> new BarleyString(BorderLayout.BEFORE_LINE_BEGINS));
        monty.put("CENTER".toLowerCase(), args -> new BarleyString(BorderLayout.CENTER));
        monty.put("EAST".toLowerCase(), args -> new BarleyString(BorderLayout.EAST));
        monty.put("LINE_END".toLowerCase(), args ->new BarleyString(BorderLayout.LINE_END));
        monty.put("LINE_START".toLowerCase(), args -> new BarleyString(BorderLayout.LINE_START));
        monty.put("NORTH".toLowerCase(), args -> new BarleyString(BorderLayout.NORTH));
        monty.put("PAGE_END.toLowerCase()",args -> new BarleyString(BorderLayout.PAGE_END));
        monty.put("PAGE_START".toLowerCase(),args -> new BarleyString(BorderLayout.PAGE_START));
        monty.put("SOUTH".toLowerCase(),args -> new BarleyString(BorderLayout.SOUTH));
        monty.put("WEST".toLowerCase(),args -> new BarleyString(BorderLayout.WEST));

        monty.put("instantiate_window", Monty::Window);
        monty.put("set_border_layout", Monty::BorderLayout);
        monty.put("set_flow_layout", Monty::FlowLayout);
        monty.put("set_grid_layout", Monty::GridLayout);
        monty.put("set_visible", Monty::SetVisible);
        monty.put("set_resizable", Monty::SetResizable);
        monty.put("new_panel", Monty::Panel);
        monty.put("text_label", Monty::Text);
        monty.put("set_size", Monty::SetSize);
        monty.put("center", Monty::Center);
        monty.put("action_button", Monty::ActionButton);
        monty.put("button", Monty::Button);
        monty.put("pack_awt", Monty::Pack);
        monty.put("slider", Monty::Slider);
        monty.put("check_box", Monty::CheckBox);
        monty.put("clear_frame", Monty::ClearFrame);
        monty.put("color", args -> new BarleyReference(new Color(args[0].asInteger().intValue(), args[1].asInteger().intValue(), args[2].asInteger().intValue(), args[3].asInteger().intValue())));
        monty.put("image_render", Monty::Image);
        monty.put("progress_bar", Monty::ProgressBar);
        monty.put("step_bar", Monty::StepBar);

        put("monty", monty);
    }

    private static void initHttp() {
        HashMap<String, Function> http = new HashMap<>();

        http.put("download", args -> {
            OkHttpClient client = new OkHttpClient();
            final Response response = client.newCall(
                            new Request.Builder().url(args[0].toString()).build())
                    .execute();
            byte[] bytes = response.body().bytes();
            LinkedList<BarleyValue> result = new LinkedList<>();
            for (byte b : bytes) {
                result.add(new BarleyNumber(b));
            }
            return new BarleyList(result);
        });

        http.put("http", new BarleyHTTP());

        put("http", http);
    }

    private static void initBase() {
        HashMap<String, Function> base = new HashMap<>();

        base.put("encode", Modules::base64encode);
        base.put("decode", Modules::base64decode);
        base.put("decode_to_string", Modules::base64encodeToString);

        put("base", base);
    }

    private static void initReflection() {
//        HashMap<String, Function> ref = new HashMap<>();
//
//        ref.put("int", args -> new BarleyReference(int.class));
//        ref.put("byte", args -> new BarleyReference(byte.class));
//        ref.put("float", args -> new BarleyReference(float.class));
//        ref.put("short", args -> new BarleyReference(short.class));
//        ref.put("long", args -> new BarleyReference(long.class));
//        ref.put("double", args -> new BarleyReference(double.class));
//        ref.put("int_arr", args -> new BarleyReference(int[].class));
//        ref.put("byte_arr", args -> new BarleyReference(byte[].class));
//        ref.put("float_arr", args -> new BarleyReference(float[].class));
//        ref.put("short_arr", args -> new BarleyReference(short[].class));
//        ref.put("long_arr", args -> new BarleyReference(long[].class));
//        ref.put("double_arr", args -> new BarleyReference(double[].class));
//
//        ref.put("class", args -> {
//            Arguments.check(1, args.length);
//            try {
//                return new BarleyReference(Class.forName(args[0].toString()));
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//            return new BarleyAtom("error");
//        });
//
//        ref.put("declared_fields", args -> {
//            Arguments.check(1, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            Field[] fields = cl.getDeclaredFields();
//            LinkedList<BarleyValue> result = new LinkedList<>();
//            for (Field field : fields) {
//                result.add(new BarleyReference(field));
//            }
//            return new BarleyList(result);
//        });
//
//        ref.put("declared_field", args -> {
//            Arguments.check(2, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            try {
//                return new BarleyReference(cl.getDeclaredField(args[1].toString()));
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            }
//            return new BarleyAtom("error");
//        });
//        ref.put("instance", args -> {
//            Arguments.checkAtLeast(1, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            Object[] const_args = List.of(args).subList(1, args.length).toArray();
//            return instance(cl.getConstructors(), const_args);
//        });
//        ref.put("fields", args -> {
//            Arguments.check(1, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            Field[] fields = cl.getFields();
//            LinkedList<BarleyValue> result = new LinkedList<>();
//            for (Field field : fields) {
//                result.add(new BarleyReference(field));
//            }
//            return new BarleyList(result);
//        });
//
//        ref.put("field", args -> {
//            Arguments.check(2, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            try {
//                return new BarleyReference(cl.getField(args[1].toString()));
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            }
//            return new BarleyAtom("error");
//        });
//
//        ref.put("declared_methods", args -> {
//            Arguments.check(1, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            Method[] methods = cl.getDeclaredMethods();
//            LinkedList<BarleyValue> result = new LinkedList<>();
//            for (Method field : methods) {
//                result.add(new BarleyReference(field));
//            }
//            return new BarleyList(result);
//        });
//
//        ref.put("methods", args -> {
//            Arguments.check(1, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            Method[] methods = cl.getMethods();
//            LinkedList<BarleyValue> result = new LinkedList<>();
//            for (Method field : methods) {
//                result.add(new BarleyReference(field));
//            }
//            return new BarleyList(result);
//        });
//
//        ref.put("method", args -> {
//            Arguments.check(2, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            try {
//                return new BarleyReference(cl.getMethod(args[1].toString()));
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
//            return new BarleyAtom("error");
//        });
//
//        ref.put("enclosing_method", args -> {
//            Arguments.check(1, args.length);
//            Class<?> cl = (((BarleyReference) args[0]).getRef()).getClass();
//            return new BarleyReference(cl.getEnclosingMethod());
//        });
//
//        ref.put("modifiers", args -> {
//            Arguments.check(1, args.length);
//            Method method = (Method) ((BarleyReference) args[0]).getRef();
//            return new BarleyNumber(method.getModifiers());
//        });
//        ref.put("is_public", args -> {
//            Arguments.check(1, args.length);
//            return new BarleyAtom(String.valueOf(Modifier.isFinal(args[0].asInteger().intValue())));
//        });
//        ref.put("is_private", args -> {
//            Arguments.check(1, args.length);
//            return new BarleyAtom(String.valueOf(Modifier.isPrivate(args[0].asInteger().intValue())));
//        });
//        ref.put("is_protected", args -> {
//            Arguments.check(1, args.length);
//            return new BarleyAtom(String.valueOf(Modifier.isProtected(args[0].asInteger().intValue())));
//        });
//        ref.put("is_transient", args -> {
//            Arguments.check(1, args.length);
//            return new BarleyAtom(String.valueOf(Modifier.isTransient(args[0].asInteger().intValue())));
//        });
//        ref.put("is_synchronized", args -> {
//            Arguments.check(1, args.length);
//            return new BarleyAtom(String.valueOf(Modifier.isSynchronized(args[0].asInteger().intValue())));
//        });
//        ref.put("is_native", args -> {
//            Arguments.check(1, args.length);
//            return new BarleyAtom(String.valueOf(Modifier.isNative(args[0].asInteger().intValue())));
//        });
//
//        ref.put("return_type", args -> {
//            Arguments.check(1, args.length);
//            Method method = (Method) ((BarleyReference) args[0]).getRef();
//            return new BarleyReference(method.getReturnType());
//        });
//
//        ref.put("generic_return_type", args -> {
//            Arguments.check(1, args.length);
//            Method method = (Method) ((BarleyReference) args[0]).getRef();
//            return new BarleyReference(method.getGenericReturnType());
//        });
//
//        ref.put("accessible", args -> {
//            Arguments.check(2, args.length);
//            Method method = (Method) ((BarleyReference) args[0]).getRef();
//            method.setAccessible(Boolean.parseBoolean(args[1].toString()));
//            return args[1];
//        });
//
//        ref.put("invoke", args -> {
//            Arguments.checkAtLeast(2, args.length);
//            Class<?> cl = (Class<?>) ((BarleyReference) args[0]).getRef();
//            Method method = (Method) ((BarleyReference) args[1]).getRef();
//            Object[] ar = List.of(args).subList(2, args.length).toArray();
//            try {
//                return new BarleyReference(method.invoke(cl, ar));
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                e.printStackTrace();
//            }
//            return new BarleyAtom("error");
//        });

        new Reflection().inject();
    }

    private static void initUnit() {
        HashMap<String, Function> unit = new HashMap<>();

        unit.put("new", args -> {
            Arguments.check(1, args.length);
            UnitBase base = Units.get(args[0].toString());
            HashMap<String, BarleyValue> fields = new HashMap<>();
            for (String f : base.getFields()) {
                fields.put(f, new BarleyAtom("not_assigned"));
            }

            for (Map.Entry<String, AST> entry : base.getDefaults().entrySet()) {
                fields.put(entry.getKey(), entry.getValue().execute());
            }
            return new BarleyReference(new Unit(fields));
        });

        unit.put("unit_to_string", args -> {
            Arguments.check(1, args.length);
            BarleyReference un = (BarleyReference) args[0];
            Unit u = (Unit) un.getRef();
            return new BarleyString(u.toString());
        });

        unit.put("set", args -> {
            Arguments.check(3, args.length);
            Unit base = (Unit) ((BarleyReference) args[0]).getRef();
            base.put(args[1].toString(), args[2]);
            return args[0];
        });

        unit.put("get", args -> {
            Arguments.check(2, args.length);
            Unit base = (Unit) ((BarleyReference) args[0]).getRef();
            BarleyValue b = base.get(args[1].toString());
            if (b == null) throw new BarleyException("BadArg", "unit '" + base + "' doesn't have key '" + args[1] + "'");
            return b;
        });


        put("unit", unit);
    }

    private static void initAnsi() {
        HashMap<String, Function> ansi = new HashMap<>();

        ansi.put("reset", args -> new BarleyString(ANSI_RESET));
        ansi.put("red", args -> new BarleyString(ANSI_RED));
        ansi.put("red_bg", args -> new BarleyString(ANSI_RED_BACKGROUND));
        ansi.put("blue", args -> new BarleyString(ANSI_BLUE));
        ansi.put("blue_bg", args -> new BarleyString(ANSI_BLUE_BACKGROUND));
        ansi.put("purple", args -> new BarleyString(ANSI_PURPLE));
        ansi.put("purple_bg", args -> new BarleyString(ANSI_PURPLE_BACKGROUND));
        ansi.put("yellow", args -> new BarleyString(ANSI_YELLOW));
        ansi.put("yellow_bg", args -> new BarleyString(ANSI_YELLOW_BACKGROUND));
        ansi.put("black", args -> new BarleyString(ANSI_BLACK));
        ansi.put("white", args -> new BarleyString(ANSI_WHITE));
        ansi.put("white_bg", args -> new BarleyString(ANSI_WHITE_BACKGROUND));

        put("ansi", ansi);
    }

    private static void initInterface() {
        HashMap<String, Function> inter = new HashMap<>();
        inter.put("vk_up", args -> new BarleyNumber(KeyEvent.VK_UP));
        inter.put("vk_down", args -> new BarleyNumber(KeyEvent.VK_DOWN));
        inter.put("vk_left", args -> new BarleyNumber(KeyEvent.VK_LEFT));
        inter.put("vk_right", args -> new BarleyNumber(KeyEvent.VK_RIGHT));
        inter.put("vk_fire", args -> new BarleyNumber(KeyEvent.VK_ENTER));
        inter.put("vk_escape", args -> new BarleyNumber(KeyEvent.VK_ESCAPE));

        inter.put("window", new CreateWindow());
        inter.put("prompt", new Prompt());
        inter.put("key_pressed", new KeyPressed());
        inter.put("mouse_hover", new MouseHover());

        inter.put("line", args -> {
            line(args[0].asInteger().intValue()
                    , args[1].asInteger().intValue()
                    , args[2].asInteger().intValue()
                    , args[3].asInteger().intValue());
            return new BarleyAtom("ok");
        });
        inter.put("oval", args -> {
            oval(args[0].asInteger().intValue()
                    , args[1].asInteger().intValue()
                    , args[2].asInteger().intValue()
                    , args[3].asInteger().intValue());
            return new BarleyAtom("ok");
        });
        inter.put("foval", args -> {
            foval(args[0].asInteger().intValue()
                    , args[1].asInteger().intValue()
                    , args[2].asInteger().intValue()
                    , args[3].asInteger().intValue());
            return new BarleyAtom("ok");
        });
        inter.put("rect", args -> {
            rect(args[0].asInteger().intValue()
                    , args[1].asInteger().intValue()
                    , args[2].asInteger().intValue()
                    , args[3].asInteger().intValue());
            return new BarleyAtom("ok");
        });
        inter.put("frect", args -> {
            frect(args[0].asInteger().intValue()
                    , args[1].asInteger().intValue()
                    , args[2].asInteger().intValue()
                    , args[3].asInteger().intValue());
            return new BarleyAtom("ok");
        });
        inter.put("clip", args -> {
            clip(args[0].asInteger().intValue()
                    , args[1].asInteger().intValue()
                    , args[2].asInteger().intValue()
                    , args[3].asInteger().intValue());
            return new BarleyAtom("ok");
        });
        inter.put("string", new DrawString());
        inter.put("color", new SetColor());
        inter.put("repaint", new Repaint());

        put("interface", inter);
    }

    private static void line(int x1, int y1, int x2, int y2) {
        graphics.drawLine(x1, y1, x2, y2);
    }

    private static void oval(int x, int y, int w, int h) {
        graphics.drawOval(x, y, w, h);
    }

    private static void foval(int x, int y, int w, int h) {
        graphics.fillOval(x, y, w, h);
    }

    private static void rect(int x, int y, int w, int h) {
        graphics.drawRect(x, y, w, h);
    }

    private static void frect(int x, int y, int w, int h) {
        graphics.fillRect(x, y, w, h);
    }

    private static void clip(int x, int y, int w, int h) {
        graphics.setClip(x, y, w, h);
    }

    private static void initLists() {
        HashMap<String, Function> lists = new HashMap<>();

        lists.put("map", args -> {
            Arguments.check(2, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            if (!(args[1] instanceof BarleyList))
                throw new BarleyException("BadArg", "Expected LIST, got " + args[1]);
            BarleyList list = (BarleyList) args[1];
            LinkedList<BarleyValue> result = new LinkedList<>();
            for (BarleyValue val : list.getList()) {
                result.add(fun.execute(val));
            }
            return new BarleyList(result);
        });

        lists.put("filter", args -> {
            Arguments.check(2, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            BarleyList list = (BarleyList) args[1];
            LinkedList<BarleyValue> result = new LinkedList<>();
            for (BarleyValue val : list.getList()) {
                if (fun.execute(val).toString().equals("true"))
                    result.add(val);
            }
            return new BarleyList(result);
        });

        lists.put("reduce", args -> {
            Arguments.check(3, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            BarleyList list = (BarleyList) args[1];
            BarleyValue acc = args[2];
            for (BarleyValue val : list.getList()) {
                acc = fun.execute(val, acc);
            }
            return acc;
        });

        lists.put("append", args -> {
            Arguments.check(2, args.length);
            BarleyList list = (BarleyList) args[0];
            LinkedList<BarleyValue> l = new LinkedList<>(list.getList());
            l.add(args[1]);
            return new BarleyList(l);

        });

        lists.put("max", args -> {
            Arguments.check(1, args.length);
            LinkedList<BarleyValue> list = ((BarleyList) args[0]).getList();
            ArrayList<Integer> ints = new ArrayList<>();
            for (BarleyValue val : list) {
                ints.add(val.asInteger().intValue());
            }
            Integer[] arr1 = ints.toArray(new Integer[]{});
            int[] arr = new int[arr1.length];
            for (int i = 0; i < arr1.length; i++) {
                arr[i] = arr1[i];
            }
            return new BarleyNumber(Arrays.stream(arr).max().getAsInt());
        });

        lists.put("min", args -> {
            Arguments.check(1, args.length);
            LinkedList<BarleyValue> list = ((BarleyList) args[0]).getList();
            ArrayList<Integer> ints = new ArrayList<>();
            for (BarleyValue val : list) {
                ints.add(val.asInteger().intValue());
            }
            Integer[] arr1 = ints.toArray(new Integer[]{});
            int[] arr = new int[arr1.length];
            for (int i = 0; i < arr1.length; i++) {
                arr[i] = arr1[i];
            }
            return new BarleyNumber(Arrays.stream(arr).min().getAsInt());
        });

        lists.put("concat", args -> {
            Arguments.check(1, args.length);
            BarleyList list = (BarleyList) args[0];
            StringBuilder acc = new StringBuilder();
            for (BarleyValue val : list.getList()) {
                acc.append(val.toString());
            }
            return new BarleyString(acc.toString());
        });

        lists.put("duplicate", args -> {
            Arguments.check(2, args.length);
            LinkedList<BarleyValue> result = new LinkedList<>();
            BarleyValue obj = args[0];
            int iteration = args[1].asFloat().intValue();
            for (int i = 0; i < iteration; i++) {
                result.add(obj);
            }
            return new BarleyList(result);
        });

        lists.put("seq", args -> {
            Arguments.check(2, args.length);
            LinkedList<BarleyValue> result = new LinkedList<>();
            BarleyValue obj = args[0];
            int iteration = args[1].asFloat().intValue();
            for (int i = 0; i < iteration; i++) {
                result.add(obj);
            }
            return new BarleyList(result);
        });

        lists.put("foreach", args -> {
            Arguments.check(2, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
            BarleyList list = (BarleyList) args[1];
            for (BarleyValue val : list.getList()) {
                fun.execute(val);
            }
            return new BarleyAtom("ok");
        });

        lists.put("last", args -> {
            Arguments.check(1, args.length);
            BarleyList list = (BarleyList) args[0];
            return list.getList().get(list.getList().size() - 1);
        });

        lists.put("nth", args -> {
            Arguments.check(2, args.length);
            BarleyList list = (BarleyList) args[0];
            int nth = args[1].asInteger().intValue();
            try {
                return list.getList().get(nth);
            } catch (IndexOutOfBoundsException ex) {
                return new BarleyAtom("end_of_list");
            }
        });

        lists.put("reverse", args -> {
            Arguments.check(1, args.length);
            LinkedList<BarleyValue> list = ((BarleyList) args[0]).getList();
            Collections.reverse(list);
            return new BarleyList(list);
        });

        lists.put("sublist", args -> {
            BarleyList list = (BarleyList) args[0];
            int from = args[1].asInteger().intValue();
            int to = args[2].asInteger().intValue();
            List<BarleyValue> subd = list.getList().subList(from, to);
            LinkedList<BarleyValue> result = new LinkedList<>(subd);
            return new BarleyList(result);
        });

        lists.put("dimension", args -> {
            int acc = 0;
            for (int i = 0; i < args.length; i++) {
                acc += args[i].asInteger().intValue();
            }
            LinkedList<BarleyValue> arr = new LinkedList<>();
            for (int i = 0; acc > i; i++) {
                arr.add(new BarleyAtom("null"));
            }
            return new BarleyList(arr);
        });

        put("lists", lists);
    }

    private static String microsToSeconds(long micros) {
        return new DecimalFormat("#0.0000").format(micros / 1000d / 1000d) + " sec";
    }

    static byte[] toPrimitives(Byte[] oBytes) {
        byte[] bytes = new byte[oBytes.length];
        for (int i = 0; i < oBytes.length; i++) {
            bytes[i] = oBytes[i];
        }
        return bytes;
    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static void put(String name, HashMap<String, Function> methods) {
        modules.put(name, methods);
    }

    public static HashMap<String, Function> get(String name) {
        return modules.get(name);
    }

    public static boolean isExists(String name) {
        return modules.containsKey(name);
    }

    private static class runTests implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            HashMap<String, Function> methods = modules.get(args[0].toString());
            List<TestInfo> tests = methods.entrySet().stream()
                    .filter(e -> e.getKey().toLowerCase().startsWith("test"))
                    .map(e -> runTest(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            int failures = 0;
            long summaryTime = 0;
            final StringBuilder result = new StringBuilder();
            for (TestInfo test : tests) {
                if (!test.isPassed) failures++;
                summaryTime += test.elapsedTimeInMicros;
                result.append("\n");
                result.append(test.info());
            }
            result.append("\n");
            result.append(String.format("Tests run: %d, Failures: %d, Time elapsed: %s",
                    tests.size(), failures,
                    microsToSeconds(summaryTime)));
            return new BarleyString(result.toString());
        }

        private TestInfo runTest(String name, Function f) {
            final long startTime = System.nanoTime();
            boolean isSuccessfull;
            String failureDescription;
            try {
                f.execute();
                isSuccessfull = true;
                failureDescription = "";
            } catch (BUnitAssertionException oae) {
                isSuccessfull = false;
                failureDescription = oae.getText();
            }
            final long elapsedTime = System.nanoTime() - startTime;
            return new TestInfo(name, isSuccessfull, failureDescription, elapsedTime / 1000);
        }
    }

    private static class BUnitAssertionException extends BarleyException {

        public BUnitAssertionException(String message) {
            super("BadTest", message);
        }
    }

    private static class TestInfo {
        String name;
        boolean isPassed;
        String failureDescription;
        long elapsedTimeInMicros;

        public TestInfo(String name, boolean isPassed, String failureDescription, long elapsedTimeInMicros) {
            this.name = name;
            this.isPassed = isPassed;
            this.failureDescription = failureDescription;
            this.elapsedTimeInMicros = elapsedTimeInMicros;
        }

        public String info() {
            return String.format("%s [%s]\n%sElapsed: %s\n",
                    name,
                    isPassed ? "passed" : "FAILED",
                    isPassed ? "" : (failureDescription + "\n"),
                    microsToSeconds(elapsedTimeInMicros)
            );
        }
    }

    private static class CanvasPanel extends JPanel {

        public CanvasPanel(int width, int height) {
            setPreferredSize(new Dimension(width, height));
            img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            graphics = img.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            setFocusable(true);
            requestFocus();
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    lastKey = new BarleyNumber(e.getKeyCode());
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    lastKey = new BarleyNumber(-1);
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mouseHover.set(0, new BarleyNumber(e.getX()));
                    mouseHover.set(1, new BarleyNumber(e.getY()));
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, null);
        }
    }

    private static class CreateWindow implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            String title = "";
            int width = 640;
            int height = 480;
            switch (args.length) {
                case 1:
                    title = args[0].toString();
                    break;
                case 2:
                    width = args[0].asInteger().intValue();
                    height = args[1].asFloat().intValue();
                    break;
                case 3:
                    title = args[0].toString();
                    width = args[1].asInteger().intValue();
                    height = args[2].asInteger().intValue();
                    break;
            }
            panel = new CanvasPanel(width, height);

            frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setVisible(true);
            return new BarleyNumber(0);
        }
    }

    private static class KeyPressed implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            return lastKey;
        }
    }

    private static class MouseHover implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            return mouseHover;
        }
    }

    private static class DrawString implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            Arguments.check(3, args.length);
            int x = args[1].asInteger().intValue();
            int y = args[2].asInteger().intValue();
            graphics.drawString(args[0].toString(), x, y);
            return new BarleyNumber(0);
        }
    }

    private static class Prompt implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            final String v = JOptionPane.showInputDialog(args[0].toString());
            return new BarleyString(v == null ? "0" : v);
        }
    }

    private static class Repaint implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            panel.invalidate();
            panel.repaint();
            return new BarleyNumber(0);
        }
    }

    private static class SetColor implements Function {

        @Override
        public BarleyValue execute(BarleyValue... args) {
            if (args.length == 1) {
                graphics.setColor(new Color(args[0].asInteger().intValue()));
                return new BarleyNumber(0);
            }
            int r = args[0].asInteger().intValue();
            int g = args[1].asInteger().intValue();
            int b = args[2].asInteger().intValue();
            graphics.setColor(new Color(r, g, b));
            return new BarleyNumber(0);
        }

    }

    static class Results {
        private int tabs = 0;
        private int spaces = 0;

        public void incTabCount() {
            ++tabs;
        }

        public void incSpaceCount() {
            ++spaces;
        }

        public int getTabCount() {
            return tabs;
        }

        public int getSpaceCount() {
            return spaces;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("tabs: ");
            sb.append(tabs);
            sb.append("\nspaces: ");
            sb.append(spaces);

            return sb.toString();
        }
    }

    private static int TYPE_URL_SAFE = 8;

    private static BarleyValue base64encode(BarleyValue... args) {
        Arguments.checkOrOr(1, 2, args.length);
        byte[] bytes = getEncoder(args).encode(getInputToEncode(args));
        LinkedList<BarleyValue> result = new LinkedList<>();
        for (byte b : bytes) {
            result.add(new BarleyNumber(b));
        }
        return new BarleyList(result);
    }

    private static BarleyValue base64encodeToString(BarleyValue... args) {
        Arguments.checkOrOr(1, 2, args.length);
        return new BarleyString(getEncoder(args).encodeToString(getInputToEncode(args)));
    }

    private static BarleyValue base64decode(BarleyValue... args) {
        Arguments.checkOrOr(1, 2, args.length);
        final Base64.Decoder decoder = getDecoder(args);
        final byte[] result;
        if (args[0] instanceof BarleyList s) {
            byte[] ar = new byte[s.getList().size()];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = s.getList().get(i).asInteger().byteValue();
            }
            result = decoder.decode(ar);
        } else {
            result = decoder.decode(args[0].toString());
            BarleyValue[] ints = new BarleyValue[result.length];
            for (int i = 0; i < result.length; i++) {
                ints[i] = new BarleyNumber(result[i]);
            }
            return new BarleyList(ints);
        }
        return null;
    }


    private static byte[] getInputToEncode(BarleyValue[] args) {
        byte[] input;
        if (args[0] instanceof BarleyList a) {
            input = new byte[a.getList().size()];
            for (int i = 0; i < a.getList().size(); i++) {
                input[i] = a.getList().get(i).asInteger().byteValue();
            }
        } else {
            try {
                input = args[0].toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                input = args[0].toString().getBytes();
            }
        }
        return input;
    }

    private static Base64.Encoder getEncoder(BarleyValue[] args) {
        if (args.length == 2 && args[1].asInteger().intValue() == TYPE_URL_SAFE) {
            return Base64.getUrlEncoder();
        }
        return Base64.getEncoder();
    }

    private static Base64.Decoder getDecoder(BarleyValue[] args) {
        if (args.length == 2 && args[1].asInteger().intValue() == TYPE_URL_SAFE) {
            return Base64.getUrlDecoder();
        }
        return Base64.getDecoder();
    }


    /**
     * Iterate the file once, checking for all desired characters,
     * and store in the Results object
     */
    private static Results countInStream(String s) throws IOException {
        InputStream is = new ByteArrayInputStream(s.getBytes("UTF8"));
        return countInStream(is);
    }

    private static Results countInStream(InputStream is) throws IOException
    {
        // create results
        Results res = new Results();
        try {
            byte[] c = new byte[1024];

            int readChars = 0;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; ++i) {
                    // see if we have a tab
                    if (c[i] == '\t') {
                        res.incTabCount();
                    }

                    // see if we have a space
                    if (c[i] == ' ') {
                        res.incSpaceCount();
                    }

                }
            }
        }
        finally {
        }

        return res;
    }

    public static boolean containsKey(Object key) {
        return modules.containsKey(key);
    }
}
