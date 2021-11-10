package com.barley.runtime;

import com.barley.utils.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Modules {

    public static HashMap<String, String> docs = new HashMap<>();
    private static final HashMap<String, HashMap<String, Function>> modules = new HashMap<>();

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
            if (!(args[0] instanceof BarleyReference))
                throw new BarleyException("BadArg", "expected REFERENCE as bts table");
            BarleyReference ref = (BarleyReference) args[0];
            HashMap<BarleyValue, BarleyValue> map = (HashMap<BarleyValue, BarleyValue>) ref.getRef();
            if (!(map.containsKey(args[1])))
                throw new BarleyException("BadArg", "map is empty or doesn't contains key '" + args[0] + "'");
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
        string.put("is_identifier", args -> {
            Arguments.check(1, args.length);
            return new BarleyAtom(String.valueOf(Character.isLetter(args[0].toString().charAt(0))));
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

        string.put("charAt", args -> {
            Arguments.check(2, args.length);
            return new BarleyString(String.valueOf(args[0].toString().charAt(args[1].asInteger().intValue())));
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

        types.put("ref_to_string",args -> {
            Arguments.check(1,args.length);
            return new BarleyString(((BarleyReference) args[0]).getRef().toString());
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
                if (m.equals(type));
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
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
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

        put("file", file);
    }

    private static void initSocket() {
        HashMap<String, Function> socket = new HashMap<>();

        socket.put("server", args -> {
            Arguments.check(1, args.length);
            try {
                return new BarleyReference(new ServerSocket(args[0].asInteger().intValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom("error");
        });

        socket.put("socket", args -> {
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
                if(global.toString().equals("globals")) continue;
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
            try  {
                String[] bts = SourceLoader.readSource(args[0].toString()).split(" ");
                List<Byte> bs = new ArrayList<>();
                for (String str : bts) {
                    bs.add(Byte.parseByte(str));
                }
                Byte[] bt = bs.toArray(new Byte[] {});
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
                    if (!parts[1].equals("=")){
                        System.err.println("Lexer Warning: Expected '=' after macros name, got '" + parts[1] + "'");
                        break;
                    }
                    String rep = String.join(" ", List.of(parts).subList(2, parts.length));
                    result += "global " + id + "\n = " + rep + "\n.\n";
                }
                List<String> rules =  List.of(lines).subList(cutAfterRules, lines.length);
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
                result += "illegal_character(S, L) -> barley:throw(\"illegal char '\" + S + \"'\").\n" +
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
        initSocket();
        initDist();
        initLists();
        initAmethyst();
    }

    private static void initLists() {
        HashMap<String, Function> lists = new HashMap<>();

        lists.put("map", args -> {
            Arguments.check(2, args.length);
            BarleyFunction fun = (BarleyFunction) args[0];
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
}
