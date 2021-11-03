package com.barley;

import com.barley.runtime.Modules;
import com.barley.utils.Handler;

public class Main {

    public static void main(String[] args) {
        Modules.init();
        Handler.loadCore();
        //Handler.tests();
        Handler.file("program.barley", true);
        Handler.handle("test:main().", true, false);
        //Handler.console();
    }
}
