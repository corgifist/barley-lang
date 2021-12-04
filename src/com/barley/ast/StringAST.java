package com.barley.ast;

import com.barley.optimizations.Optimization;
import com.barley.runtime.BarleyString;
import com.barley.runtime.BarleyValue;
import com.barley.runtime.Table;
import com.barley.utils.AST;
import com.barley.utils.Handler;

import java.io.Serializable;

public class StringAST implements AST, Serializable {

    private final String current;
    private StringBuilder result;
    private final int line;
    private final int length;
    private int pos;
    private String str;

    public StringAST(String str, int line, String current) {
        this.str = str;
        this.line = line;
        this.current = current;
        this.pos = 0;
        this.length = str.length();
        this.result = new StringBuilder();
    }

    @Override
    public BarleyValue execute() {
        lex();

        return new BarleyString(result.toString());
    }

    private void lex() {
        while (pos < length) {
            char c = peek(0);
            switch (c) {
                case '#':
                    next();
                    interpolate();
                    break;

                default:
                    result.append(c);
                    next();
            }
        }
    }

    private void interpolate() {
        char c = next();
        StringBuilder buffer = new StringBuilder();
        while (c != '}') {
            buffer.append(c);
            c = next();
        }
        next();
        result.append(Handler.evalAST(buffer + "."));
    }

    private char peek(int relativePos) {
        int p = relativePos + pos;
        if (p >= length) return '\0';
        return str.charAt(p);
    }

    private char next() {
        pos++;
        return peek(0);
    }

    @Override
    public void visit(Optimization optimization) {

    }
}
