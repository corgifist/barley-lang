package com.barley.parser;

public enum TokenType {

    INT,
    STRING,
    FLOAT,

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

    VAR , ATOM, EOF
}
