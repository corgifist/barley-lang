package com.barley.utils;

import com.barley.parser.Lexer;

import java.util.List;

public class Handler {
    public static void handle(String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
