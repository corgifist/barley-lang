package com.barley.parser;

public enum TokenType {

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

    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    LBRACKET,
    RBRACKET,

    DOT,

    MODULE,
    WHEN,
    RECIEVE,

    VAR , ATOM, EOF
}
