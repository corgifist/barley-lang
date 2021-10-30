package com.barley.runtime;

import com.barley.ast.ExtractBindAST;
import com.barley.ast.JavaFunctionAST;
import com.barley.utils.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Modules {

    private static HashMap<String, HashMap<String, Function>> modules = new HashMap<>();

    static {
        HashMap<String, Function> shell = new HashMap<>();
        shell.put("reparse", (args -> {
            try {
                Handler.handle(SourceLoader.readSource(args[0].toString()), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new BarleyAtom(AtomTable.put("ok"));
        }));
        shell.put("f", (args -> {
            Table.clear();
            return new BarleyAtom(AtomTable.put("ok"));
        }));
        shell.put("b", args -> {
            System.out.println(Table.variables());
            return new BarleyAtom(AtomTable.put("ok"));
        });
        shell.put("q", args -> {
            System.exit(0);
            return new BarleyAtom(AtomTable.put("exit"));
        });
        shell.put("spawn", args -> {
            PidValues pid = new PidValues(getRandomNumber(0, 300), getRandomNumber(0, 300), getRandomNumber(0, 300));
            BarleyPID p = new BarleyPID(pid);
            ProcessTable.put(p);
            return p;
        });
        put("barley", shell);
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
