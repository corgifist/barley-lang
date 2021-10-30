package com.barley;

import com.barley.parser.Lexer;
import com.barley.utils.Handler;

public class Main {

    public static void main(String[] args) {
        Handler.file("program.barley");
        Handler.console();
    }
}
