package com.barley.runtime;

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
            if (!(map.containsKey(args[1]))) throw new BarleyException("BadArg", "map is empty or doesn't contains key '" + args[0] + "'");
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
        shell.put("from_binary", args -> {
            Arguments.check(1, args.length);
            LinkedList<BarleyValue> arr = ((BarleyList) args[0]).getList();
            ArrayList<Byte> b = new ArrayList<>();
            for (BarleyValue bt : arr) {
                b.add((byte) bt.asInteger().intValue());
            }
            Byte[] bs = b.toArray(new Byte[] {});
            byte[] binary = toPrimitives(bs);
            try {
                return SerializeUtils.deserialize(binary);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
            return list.getList().get(nth);
        });
        shell.put("sublist", args -> {
            BarleyList list = (BarleyList) args[0];
            int from = args[1].asInteger().intValue();
            int to = args[2].asInteger().intValue();
            LinkedList<BarleyValue> result = new LinkedList<>();
            List<BarleyValue> subd = list.getList().subList(from, to);
            for (BarleyValue value : subd) {
                result.add(value);
            }
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
                        writer.append(String.valueOf(b) + " ");
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
                byte[] binary = toPrimitives(bts.toArray(new Byte[] {}));
                List<AST> ast = (List<AST>) SerializeUtils.deserialize(binary);
                System.out.println("after parsing");
                for (AST node : ast) {
                    node.execute();
                }
                return new BarleyAtom(AtomTable.put("ok"));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
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
                return fun.execute(new BarleyValue[]{});
            } catch (BarleyException ex) {
                return c.execute(new BarleyValue[]{new BarleyAtom(ex.getType().toLowerCase(Locale.ROOT))});
            }
        });

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

        modules.put("math", math);
    }

    private static void initString() {
        HashMap<String, Function> string = new HashMap<>();

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
        string.put("split", args -> {
            Arguments.checkOrOr(1, 2, args.length);
            LinkedList<BarleyValue> result = new LinkedList<>();
            String str = args[0].toString();
            switch (args.length) {
                case 1:
                    String[] parts = str.split(" ");
                    for (String part : parts) {
                        result.add(new BarleyString(part));
                    }
                    return new BarleyList(result);
                case 2:
                    String[] parts_ = str.split(args[1].toString());
                    for (String part : parts_) {
                        result.add(new BarleyString(part));
                    }
                    return new BarleyList(result);
                default: throw new BarleyException("BadArg", "unexpected error was occurred");
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
            if (!(s instanceof BarleyReference)) throw new BarleyException("BadArg", "expected reference as stack object");
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
            if (!(s instanceof BarleyReference)) throw new BarleyException("BadArg", "expected reference as stack object");
            Stack<BarleyValue> st = (Stack<BarleyValue>) ((BarleyReference) s).getRef();
            return st.peek();
        });

        stack.put("stack_to_list", args -> {
            Arguments.check(1, args.length);
            BarleyValue s = args[0];
            if (!(s instanceof BarleyReference)) throw new BarleyException("BadArg", "expected reference as stack object");
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

        types.put("is_string", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(AtomTable.put(String.valueOf(args[0] instanceof BarleyString)));
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

        put("types", types);
    }

    public static void init() {
        initBarley();
        initIo();
        initBts();
        initMath();
        initString();
        initStack();
        initTypes();
    }

    static byte[] toPrimitives(Byte[] oBytes)
    {
        byte[] bytes = new byte[oBytes.length];
        for(int i = 0; i < oBytes.length; i++){
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

}
