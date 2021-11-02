package com.barley.parser;

import com.barley.utils.BarleyException;
import com.barley.utils.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class Lexer {

    private static final String OPERATOR_CHARS = "+-*/()!<>=;{}:#[],.|";
    private static final Map<String, TokenType> OPERATORS;
    static {
        OPERATORS = new HashMap<>();
        OPERATORS.put("+", TokenType.PLUS);
        OPERATORS.put("-", TokenType.MINUS);
        OPERATORS.put("*", TokenType.STAR);
        OPERATORS.put("/", TokenType.SLASH);
        OPERATORS.put("(", TokenType.LPAREN);
        OPERATORS.put(")", TokenType.RPAREN);
        OPERATORS.put("=", TokenType.EQ);
        OPERATORS.put(";", TokenType.SEMICOLON);
        OPERATORS.put("!", TokenType.BANG);
        OPERATORS.put("!=", TokenType.BANGEQ);
        OPERATORS.put("==", TokenType.EQEQ);
        OPERATORS.put(">", TokenType.GT);
        OPERATORS.put(">=", TokenType.GTEQ);
        OPERATORS.put("<", TokenType.LT);
        OPERATORS.put("<=", TokenType.LTEQ);
        OPERATORS.put("{", TokenType.LBRACE);
        OPERATORS.put("}", TokenType.RBRACE);
        OPERATORS.put(":", TokenType.COLON);
        OPERATORS.put("|", TokenType.BAR);
        OPERATORS.put("#", TokenType.SHARP);
        OPERATORS.put("[", TokenType.LBRACKET);
        OPERATORS.put("]", TokenType.RBRACKET);
        OPERATORS.put(",", TokenType.COMMA);
        OPERATORS.put("->", TokenType.STABBER);
        OPERATORS.put(".", TokenType.DOT);
        OPERATORS.put("||", TokenType.BARBAR);
        OPERATORS.put("::", TokenType.CC);
    }

    private static final Map<String, TokenType> KEYWORDS;
    static {
        KEYWORDS = new HashMap<>();
        KEYWORDS.put("module", TokenType.MODULE);
        KEYWORDS.put("doc", TokenType.MODULEDOC);
        KEYWORDS.put("when", TokenType.WHEN);
        KEYWORDS.put("receive", TokenType.RECIEVE);
        KEYWORDS.put("case", TokenType.CASE);
        KEYWORDS.put("of", TokenType.OF);
        KEYWORDS.put("end", TokenType.END);
        KEYWORDS.put("and", TokenType.AND);
        KEYWORDS.put("or", TokenType.OR);
        KEYWORDS.put("global", TokenType.GLOBAL);
        KEYWORDS.put("not", TokenType.NOT);
    }

    private final String input;
    private final int length;
    private int line = 1;

    private final List<Token> tokens;

    private int pos;

    public Lexer(String input) {
        this.input = input;
        length = input.length();

        tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (pos < length) {
            char current = peek(0);
            if (Character.isDigit(current)) tokenizeNumber();
            else if (Character.isLetterOrDigit(current)) tokenizeWord();
            else if (current == '"') tokenizeString();
            else if (OPERATOR_CHARS.indexOf(current) != -1) {
                tokenizeOperator();
            } else if (current == '%') {
                tokenizeComment();
            } else {
                // whitespaces
                next();
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void tokenizeComment() {
        char current = peek(0);
        while ("\r\n\0".indexOf(current) == -1) {
            current = next();
        }
    }

    private void tokenizeString() {
        StringBuilder buffer = new StringBuilder();

        next();// skip "
        char current = peek(0);
        while (true) {
            if (current == '\\') {
                current = next();
                switch (current) {
                    case '"': current = next(); buffer.append('"'); continue;
                    case '0': current = next(); buffer.append('\0'); continue;
                    case 'b': current = next(); buffer.append('\b'); continue;
                    case 'f': current = next(); buffer.append('\f'); continue;
                    case 'n': current = next(); buffer.append('\n'); continue;
                    case 'r': current = next(); buffer.append('\r'); continue;
                    case 't': current = next(); buffer.append('\t'); continue;
                    case 'u': // http://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.3
                        int rollbackPosition = pos;
                        while (current == 'u') current = next();
                        int escapedValue = 0;
                        for (int i = 12; i >= 0 && escapedValue != -1; i -= 4) {
                            if (isHexNumber(current)) {
                                escapedValue |= (Character.digit(current, 16) << i);
                            } else {
                                escapedValue = -1;
                            }
                            current = next();
                        }
                        if (escapedValue >= 0) {
                            buffer.append((char) escapedValue);
                        } else {
                            // rollback
                            buffer.append("\\u");
                            pos = rollbackPosition;
                        }
                        continue;
                }
                buffer.append('\\');
                continue;
            }
            if (current == '"') break;
            if (current == '\0') throw new BarleyException("BadCompiler", "Reached end of file while parsing text string");
            buffer.append(current);
            current = next();
        }
        next(); // skip closing "

        addToken(TokenType.STRING, buffer.toString());
    }

    private void tokenizeWord() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(peek(0));
        char current = next();
        while (true) {
            if (!Character.isLetterOrDigit(current) && current != '_') {
                break;
            }
            buffer.append(current);
            current = next();
        }

        final String word = buffer.toString();
        if (KEYWORDS.containsKey(word)) {
            addToken(KEYWORDS.get(word));
        } else {
            if (isStringLowerCase(word)) addToken(TokenType.ATOM, word);
            else addToken(TokenType.VAR, word);
        }
    }

    private void tokenizeNumber() {
        final StringBuilder buffer = new StringBuilder();
        char current = peek(0);
        while (true) {
            if (current == '.' && Character.isDigit(peek(1))) {
                if (buffer.indexOf(".") != -1) throw new BarleyException("BadCompiler", "Invalid float number");
            } else if (!Character.isDigit(current)) {
                break;
            }
            buffer.append(current);
            current = next();
        }
        addToken(TokenType.NUMBER, buffer.toString());
    }


    private static boolean isHexNumber(char current) {
        return "abcdef".indexOf(Character.toLowerCase(current)) != -1;
    }

    private void tokenizeOperator() {
        char current = peek(0);
        final StringBuilder buffer = new StringBuilder();
        while (true) {
            final String text = buffer.toString();
            if (!OPERATORS.containsKey(text + current) && !text.isEmpty()) {
                addToken(OPERATORS.get(text));
                return;
            }
            buffer.append(current);
            current = next();
        }
    }

    private char next() {
        pos++;
        final char result = peek(0);
        if (result == '\n') {
            line++;
        }
        return result;
    }

    private char peek(int relativePosition) {
        final int position = pos + relativePosition;
        if (position >= length) return '\0';
        return input.charAt(position);
    }

    private void addToken(TokenType type) {
        addToken(type, "");
    }

    private void addToken(TokenType type, String text) {
        tokens.add(new Token(type, text, line));
    }

    private static boolean isStringLowerCase(String str){
        char[] charArray = str.toCharArray();

        for(int i=0; i < charArray.length; i++){
            if( !Character.isLowerCase(charArray[i]) && !(charArray[i] == '_'))
                return false;
        }
        return true;
    }
}
