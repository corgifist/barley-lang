package com.barley.parser;

import java.io.Serializable;

public enum TokenType implements Serializable {

    NUMBER,
    STRING,

    PLUS,
    MINUS,
    STAR,
    SLASH,
    BANG,
    SEMICOLON,
    EQ,
    COLON,
    SHARP,
    COMMA,
    STABBER,

    BANGEQ,
    EQEQ,
    GT,
    GTEQ,
    LT,
    LTEQ,
    BAR,
    BARBAR,
    CC,

    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    LBRACKET,
    RBRACKET,

    DOT,

    MODULE,
    MODULEDOC,
    WHEN,
    RECIEVE,
    CASE, OF, END,
    AND, OR, GLOBAL,
    NOT, DEF,

    VAR, ATOM, EOF
}
