package com.esjr.bluejay;

import java.util.*;
import static com.esjr.bluejay.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {}
    
    final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        // The entire program is just a list of statements, we go through them here.
        List<Stmt> stmts = new ArrayList<Stmt>();
        while (!isAtEnd()) {
            stmts.add(statement());
        }
        return stmts;
    }

    private Stmt statement() {
        // This function corresponds to the `stmt` rule in the grammar.
        try {
            // Parsing a statement starts here -- we branch off into other functions.
        } catch (ParseError e) {
            // Go into panic mode if we see an error
            synchronize();
            return null;
        }
    }

    private void synchronize() {
        /* Goes into panic mode -- we know the code has an error,
        so we're never going to try to run it, but we should still
        try to find as many errors as possible but we don't want
        to create 'phantom' errors by continuing to parse errenuous
        code.
        
        To do this, we get back to a spot where we know we can't
        trigger a phantom error -- the start of a statement! So
        we just advance until we see either a semicolon or a token
        we know starts a statemsnt. This way, we find as many errors
        as possible without creating phantoms. */

        advance();
        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch (peek().type) {
                // add cases for every token we know starts a statement (IF, WHILE, etc.)
                    // return;
            }
            advance();
        }
    }

    // The following functions are easy to figure out, so I've ommitted comments.

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        // Both disply the error and force the parser into panic mode.
        Bluejay.error(token, message);
        return new ParseError();
    }
}