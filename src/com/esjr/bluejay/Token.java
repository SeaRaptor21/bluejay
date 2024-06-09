package com.esjr.bluejay;

class Token {
    final TokenType type;
    final String lexeme;
    final Value literal;
    final int pos; 

    Token(TokenType type, String lexeme, Value literal, int pos) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.pos = pos;
    }

    Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = null;
        this.pos = 0;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}