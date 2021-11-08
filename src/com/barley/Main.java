package com.barley;

import com.barley.runtime.Modules;
import com.barley.utils.Handler;

public class Main {

    public static void main(String[] args) {
        Modules.init();
        //Handler.tests();
        //Handler.magicBall();
        Handler.entry("examples/chat.barley");
        //Handler.console();
    }
}
