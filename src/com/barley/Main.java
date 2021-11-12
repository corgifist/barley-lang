package com.barley;

import com.barley.runtime.BarleyString;
import com.barley.runtime.Modules;
import com.barley.utils.Handler;
import com.barley.utils.SourceLoader;

import javax.xml.transform.Source;
import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Modules.init();
        if (args.length == 0) {
            Handler.console();
        }

        String file = args[0];
        if (file.equals("-entry")) {
            System.out.println(List.of(args));
            Handler.entry(args[1], args[2]);
            return;
        }
        if (file.equals("-tests")) {
            Handler.tests();
            return;
        }
        String[] dotParts = file.split("\\.");
        if (dotParts[dotParts.length - 1].equals("app")) {
            Modules.get("dist").get("app").execute(new BarleyString(file));
        } else {
            try {
                Handler.handle(SourceLoader.readSource(file), false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
