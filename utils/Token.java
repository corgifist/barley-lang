package com.barley.utils;

import com.barley.parser.TokenType;

import java.io.Serializable;

public final class Token implements Serializable {

    private TokenType type;
    private String text;
    private int line;

    public Token() {
    }

    public Token(TokenType type, String text, int line) {
        this.type = type;
        this.text = text;
        this.line = line;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return type + " " + text;
    }
}
