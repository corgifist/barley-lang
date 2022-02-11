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
    LTLT,
    GTGT,
    UNBIN,

    LPAREN, // (
    RPAREN, // )
    LBRACE, // {
    RBRACE, // }
    LBRACKET,
    RBRACKET,
    QUESTION,
    POINT, UNPOINT,

    DOT,

    MODULE,
    MODULEDOC,
    WHEN,
    RECIEVE,
    CASE, OF, END,
    AND, OR, GLOBAL,
    NOT, DEF, OPT, DEFGUARD, UNIT, STRICT, PACK, UNPACK, AST, EXTERN,
    BINARY_OPERATION, UNARY_OPERATION,

    VAR, ATOM, NUM, DOG, DOL, EOF
}
