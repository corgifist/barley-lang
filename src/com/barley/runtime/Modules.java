package com.barley.runtime;

import com.barley.ast.ExtractBindAST;
import com.barley.ast.JavaFunctionAST;
import com.barley.utils.*;

import java.io.*;
import java.util.*;

public class Modules {

    private static HashMap<String, HashMap<String, Function>> modules = new HashMap<>();

    public static HashMap<String, String> docs = new HashMap<>();

    static {

    }
    private static void initIo() {
        HashMap<String, Function> io = new HashMap<>();
        io.put("write", args -> {
            Arguments.check(1, args.length);
            System.out.print(args[0].toString());
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("writeln", args -> {
            Arguments.check(1, args.length);
            System.out.println(args[0].toString());
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("format", args -> {
            Arguments.checkAtLeast(1, args.length);

            final String format = args[0].toString();
            final Object[] values = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                values[i - 1] = args[i].toString();
            }
            return new BarleyString(String.format(format, values));
        });
        io.put("fwrite", args -> {
            Arguments.checkAtLeast(1, args.length);

            final String format = args[0].toString();
            final Object[] values = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                values[i - 1] = args[i].toString();
            }
            System.out.print(String.format(format, values));
            return new BarleyAtom(AtomTable.put("ok"));
        });
        io.put("fwriteln", args -> {
            Arguments.checkAtLeast(1, args.length);

            final String format = args[0].toString();
            final Object[] values = new Object[args.length - 1];
            for (int i = 1; i < args.length; i++) {
                values[i - 1] = args[i].toString();
            }
            System.out.println(String.format(format, values));
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
            if (!(args[0] instanceof BarleyReference)) throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            ((HashMap<BarleyValue, BarleyValue>) ref.getRef()).put(args[1], args[2]);
            return ref;
        });
        bts.put("tabtolist", args -> {
            Arguments.check(1, args.length);
            if (!(args[0] instanceof BarleyReference)) throw new BarleyException("BadArg", "expected REFERENCE as bts table");
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

        bts.put("member", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference)) throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            return new BarleyAtom(AtomTable.put(String.valueOf(map.containsKey(args[1]))));
        });
        bts.put("lookup", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference)) throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            return map.get(args[1]);
        });
        bts.put("remove", args -> {
            Arguments.check(2, args.length);
            if (!(args[0] instanceof BarleyReference)) throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            map.remove(args[1]);
            return ref;
        });

        put("bts", bts);
    }

    private static void initBarley() {
        HashMap<String, Function> shell = new HashMap<>();

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
                case 0:
                    ProcessTable.put(p);
                    return p;
                case 1:
                    ProcessTable.put(p, args[0]);
                    return p;
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
                bytes = SerializeUtils.serialize((Serializable) args[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < bytes.length; i++) {
                result.add(new BarleyNumber(bytes[i]));
            }
            return new BarleyList(result);
        });
        shell.put("docs", args -> {
            Arguments.check(1, args.length);
            String module = args[0].toString();
            System.out.println(docs.get(module));
            return new BarleyAtom(AtomTable.put("ok"));
        });
        put("barley", shell);
    }

    public static void init() {
        initBarley();
        initIo();
        initBts();
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

}
