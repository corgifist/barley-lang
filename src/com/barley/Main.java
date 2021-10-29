package com.barley;

import com.barley.parser.Lexer;

public class Main {

    public static void main(String[] args) {
        Lexer lexer = new Lexer("2 + 2");
        System.out.println(lexer.tokenize());
    }
}
