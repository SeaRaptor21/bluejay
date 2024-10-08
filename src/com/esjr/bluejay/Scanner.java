package com.esjr.bluejay;

import java.util.*;
import static com.esjr.bluejay.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    @SuppressWarnings("unused")
    private int line = 1;
    private int grouping = 0;
    private static final Map<String, TokenType> keywords;
    private static final Set<TokenType> implicitContinuation = Set.of(
        EOS,
        COMMA, DOT, MINUS, PLUS, SLASH, STAR, STAR_STAR, PERCENT, EQUAL,
        PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, STAR_STAR_EQUAL, PERCENT_EQUAL,
        LEFT_BRACE, LEFT_PAREN, LEFT_SQUARE,
        BANG, BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
        AND, CLASS, ELSE, FUNC, FOR, IF, NOT, OR, VAR, WHILE, XOR
        /*  NOTE: Keywords are put in here. 
            This allows things such as:
            ```
            if
            (x == 5) { ... }
            ```
            Do we want this? I'm honstly not sure.
        */
    );

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("break",   BREAK);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("foreach", FOREACH);
        keywords.put("func",    FUNC);
        keywords.put("if",      IF);
        keywords.put("import",  IMPORT);
        keywords.put("in",      IN);
        keywords.put("not",     NOT);
        keywords.put("null",    NULL);
        keywords.put("or",      OR);
        keywords.put("repeat",  REPEAT);
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
        keywords.put("xor",     XOR);
    }


    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        if (tokens.size() > 0 && tokens.get(tokens.size()-1).type != EOS) {
            tokens.add(new Token(EOS, "", null, start));
        }

        tokens.add(new Token(EOF, "", null, start));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                grouping++;
                break;
            case ')':
                addToken(RIGHT_PAREN);
                grouping--;
                break;
            case '[':
                addToken(LEFT_SQUARE);
                grouping++;
                break;
            case ']':
                addToken(RIGHT_SQUARE);
                grouping--;
                break;
            case '{':
                addToken(LEFT_BRACE);
                //grouping++;
                break;
            case '}':
                addToken(RIGHT_BRACE);
                //grouping--;
                break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-':
                addToken(match('-') ? MINUS_MINUS : (match('=') ? MINUS_EQUAL : MINUS));
                break;
            case '%':
                addToken(match('=') ? PERCENT_EQUAL : PERCENT);
                break;
            case '+':
                addToken(match('+') ? PLUS_PLUS : (match('=') ? PLUS_EQUAL : PLUS));
                break;
            case ';': addToken(EOS); break;
            case '*':
                addToken(match('*') ? (match('=') ? STAR_STAR_EQUAL : STAR_STAR) : (match('=') ? STAR_EQUAL : STAR));
                break; 
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    while ((peek() != '*' || peekNext() != '/') && !isAtEnd()) advance();
                    if (!isAtEnd()) {
                        advance();
                        advance();
                    }
                } else {
                    addToken(match('=') ? SLASH_EQUAL : SLASH);
                }
                break;
            case ':': addToken(COLON); break;
            case '"': string(); break;
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                if (!(grouping>0) && !tokens.isEmpty() && !implicitContinuation.contains(tokens.get(tokens.size()-1).type)) {
                    addToken(EOS);
                }
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Bluejay.error(start, "Unexpected character.");
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Bluejay.error(current-1, "Unterminated string.");
            return;
        }

        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STR, new Value.BluejayString(value));
    }

    private void number() {
        while (isDigit(peek())) advance();
        
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUM, new Value.Number(Double.parseDouble(source.substring(start, current))));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = ID;
        addToken(type);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }
    
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Value literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, start));
    }
}