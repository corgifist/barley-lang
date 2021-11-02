package com.barley;

import com.barley.parser.Lexer;
import com.barley.runtime.AtomTable;
import com.barley.runtime.Modules;
import com.barley.utils.Handler;

public class Main {

    public static void main(String[] args) {
        Modules.init();
        Handler.loadCore();
        Handler.file("program.barley", true);
        Handler.handle("test:main().", true, false);
        Handler.console();
    }
}
