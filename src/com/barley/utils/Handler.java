package com.barley.utils;

import com.barley.parser.Lexer;
import com.barley.parser.Parser;

import javax.naming.PartialResultException;
import java.util.List;

public class Handler {
    public static void handle(String input) {
        try {
            Lexer lexer = new Lexer(input);
            List<Token> tokens = lexer.tokenize();
            for (Token token : tokens) {
                System.out.println(token);
            }
            List<AST> nodes = new Parser(tokens).parse();
            for (AST node : nodes) {
                System.out.println(node.execute());
            }
        } catch (BarleyException ex) {
            System.out.printf("** exception error: %s", ex.getText());
        }
    }
}
