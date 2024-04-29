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
            if (match(LEFT_BRACE)) return block();
            return exprStmt();
        } catch (ParseError e) {
            // Go into panic mode if we see an error
            synchronize();
            return null;
        }
    }

    private Stmt block() {
        List<Stmt> stmts = new ArrayList<Stmt>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(statement());
        }
        consume(RIGHT_BRACE, "Expected closing '}' after block.");
        return new Stmt.Block(stmts);
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        consume(EOS, "Expected newline or semicolon to end statement.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        if (check(ID) && checkNext(EQUAL)) return assign();
        return or();
    }

    private Expr.Assign assign() {
        // I'm using advance instead of return because we already
        // know these are here from the checks in expression.
        Token name = advance();
        Token operator = advance();
        if (operator.type == PLUS_PLUS || operator.type == MINUS_MINUS) return new Expr.Assign(name, operator, null);
        Expr value = expression();
        return new Expr.Assign(name, operator, value);
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR, XOR)) {
            Token op = previous();
            Expr right = and();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = not();
        while (match(AND)) {
            Token op = previous();
            Expr right = not();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr not() {
        if (match(NOT)) {
            Token op = previous();
            Expr right = not();
            return new Expr.Unary(op, right);
        } else {
            return equal();
        }
    }

    private Expr equal() {
        Expr expr = compare();
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token op = previous();
            Expr right = compare();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr compare() {
        Expr expr = term();
        while (match(GREATER, LESS, GREATER_EQUAL, LESS_EQUAL)) {
            Token op = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token op = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = pow();
        while (match(SLASH, STAR, PERCENT)) {
            Token op = previous();
            Expr right = pow();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr pow() {
        Expr expr = unary();
        while (match(STAR_STAR)) {
            Token op = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token op = previous();
            Expr right = unary();
            return new Expr.Unary(op, right);
        } else {
            return call();
        }
    }

    private Expr call() {
        Expr expr = primary();
        while (match(LEFT_PAREN, DOT, LEFT_SQUARE)) {
            if (previous().type == LEFT_PAREN) {
                List<Expr> args = new ArrayList<Expr>();
                if (!check(RIGHT_PAREN)) {
                    args.add(expression());
                    while (match(COMMA)) {
                        args.add(expression());
                    }
                }
                consume(RIGHT_PAREN, "Expected ')' after function call.");
                expr = new Expr.Call(expr, args);
            } else if (previous().type == DOT) {
                Token name = consume(ID, "Expected attribute or method name after '.'.");
                expr = new Expr.Attr(expr, name);
            } else {
                Expr index = expression();
                consume(RIGHT_SQUARE, "Expected closing ']' after index.");
                expr = new Expr.Index(expr, index);
            }
        }
        return expr;
    }

    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(NULL)) return new Expr.Literal(null);
        if (match(THIS, ID)) return new Expr.Var(previous());
        if (match(NUM, STR)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expected closing ')' after expression.");
            return new Expr.Grouping(expr);
        }
        if (match(LEFT_SQUARE)) {
            List<Object> elems = new ArrayList<Object>();
            if (!check(RIGHT_SQUARE)) {
                elems.add(expression());
                while (match(COMMA)) {
                    elems.add(expression());
                }
            }
            consume(RIGHT_SQUARE, "Expected ']' after list elements.");
            return new Expr.List(elems);
        }
        if (match(LEFT_BRACE)) {
            List<Object> keys = new ArrayList<Object>();
            List<Object> vals = new ArrayList<Object>();
            if (!check(RIGHT_BRACE)) {
                do {
                    keys.add(expression());
                    consume(COLON, "Expected ':' after dictionary key.");
                    vals.add(expression());
                } while (match(COMMA))
            }
            consume(RIGHT_BRACE, "Expected '}' after dictionary elements.");
            return new Expr.Dict(keys, vals);
        }
        throw error(peek(), "Expression expected.");
    }

    private void synchronize() {
        /* Goes into panic mode -- we know the code has an error,
        so we're never going to try to run it, but we should still
        try to find as many errors as possible but we don't want
        to create 'phantom' errors by continuing to parse errenuous
        code.
        
        To do this, we get back to a spot where we know we can't
        trigger a phantom error -- the start of a new statement. So
        we just advance until we see either a semicolon or a token
        we know starts a statement. This way, we find as many errors
        as possible without creating phantoms. */

        advance();
        while (!isAtEnd()) {
            if (previous().type == EOS) return;
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

    private boolean checkNext(TokenType... types) {
        return checkNext(types, 1);
    }

    private boolean checkNext(TokenType... types, int amount) {
        if (isAtEnd()) return false;
        return Arrays.asList(types).contains(peekNext(amount).type);
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

    private Token peekNext() {
        return peekNext(1);
    }

    private Token peekNext(int amount) {
        return tokens.get(current+amount);
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