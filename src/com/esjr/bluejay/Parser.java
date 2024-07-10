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
            // Get rid of any unneccisary newlines that could cause problems.
            while (check(EOS) && (peek().lexeme.contains(System.getProperty("line.separator")) || peek().lexeme == "")) {
                advance();
            }
        }
        return stmts;
    }

    private Stmt statement() {
        // This function corresponds to the `stmt` rule in the grammar.
        try {
            if (match(LEFT_BRACE)) return block();
            if (match(IF)) return ifStmt();
            if (match(WHILE)) return whileStmt();
            if (match(FOR)) return forStmt();
            if (match(FOREACH)) return foreachStmt();
            if (match(REPEAT)) return repeatStmt();
            if (match(BREAK)) return breakStmt();
            if (match(RETURN)) return returnStmt();
            if (match(VAR)) return varStmt();
            if (match(FUNC)) return functionStmt();
            if (match(CLASS)) return classStmt();
            if (match(IMPORT)) return importStmt();
            return exprStmt();
        } catch (ParseError e) {
            // Go into panic mode if we see an error
            synchronize();
            return null;
        }
    }

    private Stmt exprStmt() {
        Expr expr = expression();
        consume(EOS, "Expected newline or semicolon to end statement.");
        return new Stmt.Expression(expr);
    }

    private Stmt block() {
        List<Stmt> stmts = new ArrayList<Stmt>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            stmts.add(statement());
        }
        consume(RIGHT_BRACE, "Expected closing '}' after block.");
        // Eat any newlines that could cause problems later.
        eatNewlines();
        return new Stmt.Block(stmts);
    }

    private Stmt ifStmt() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(cond, thenBranch, elseBranch);
    }

    private Stmt whileStmt() {
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr cond = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition.");
        Stmt stmt = statement();
        return new Stmt.While(cond, stmt);
    }

    private Stmt forStmt() {
        // There's probably a whole host of bugs here
        // because I'm checking for a semicolon specifically,
        // but I want to get this to you fast.
        consume(LEFT_PAREN, "Expected '(' after 'for'.");
        Stmt init = null;
        if (match(VAR)) {
            init = varStmt();
        } else if (matchSemicolon()) {
            init = null;
        } else {
            init = exprStmt();
        }
        Expr cond = null;
        if (!checkSemicolon()) {
            cond = expression();
        }
        consumeSemicolon();
        Expr inc = null;
        if (!check(RIGHT_PAREN)) {
            inc = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for increment expression.");
        Stmt stmt = statement();
        /* This is our first example of desugaring!

        As you know, a for loop can easily be rewritten
        as a while loop, and that the for syntax is merely
        syntactic sugar. So instead of worrying about for
        loops inside of the interpreter, they're often handled
        directly in the parser. To do this, all we have to do
        is create that while loop that the for loop describes;
        in other words, we need to go from
        ```
        for (<init>; <cond>; <inc>) <body>
        ```
        to 
        ```
        {
            <init>;
            while (<cond>) {
                <body>
                <inc>
            }
        }
        ```
        This way we don't need to add anything to the interpreter.
        Look closely at the following 2 lines to see an example
        of how this works.
        */
        Stmt block = new Stmt.Block(Arrays.asList(stmt, new Stmt.Expression(inc)));
        return new Stmt.Block(Arrays.asList(init, new Stmt.While(cond, block)));
    }

    private Stmt foreachStmt() {
        consume(LEFT_PAREN, "Expected '(' after 'foreach'.");
        consume(VAR, "Expected 'var' inside foreach loop.");
        Token loopVar = consume(ID, "Expected loop variable name.");
        consume(IN, "Expected 'in' after loop variable name.");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expected ')' after foreach expression.");
        Stmt body = statement();
        return new Stmt.Foreach(loopVar, expr, body);
    }

    private Stmt repeatStmt() {
        Token paren = consume(LEFT_PAREN, "Expected '(' after 'repeat'.");
        Expr amount = expression();
        consume(RIGHT_PAREN, "Expected ')' after repeat amount.");
        Stmt body = statement();
        return new Stmt.Repeat(paren, amount, body);
    }

    private Stmt breakStmt() {
        Token kwd = previous();
        if (match(EOS)) {
            return new Stmt.Break(kwd, null);
        }
        Expr amount = expression();
        consume(EOS, "Expected neweline or semicolon after break statement.");
        return new Stmt.Break(kwd, amount);
    }

    private Stmt returnStmt() {
        Token kwd = previous();
        if (match(EOS)) {
            return new Stmt.Return(kwd, null);
        }
        Expr expr = expression();
        consume(EOS, "Expected neweline or semicolon after return statement.");
        return new Stmt.Return(kwd, expr);
    }

    private Stmt varStmt() {
        Token name = consume(ID, "Expected vaiable name.");
        Expr init = null;
        if (match(EQUAL)) {
            init = expression();
        }
        consume(EOS, "Expected newline or semicolon after variable declaration.");
        return new Stmt.Var(name, init);
    }

    private Stmt functionStmt() {
        Token name = consume(ID, "Expected function name.");
        consume(LEFT_PAREN, "Expected '(' after function name.");
        LinkedHashMap<Token,Object> params = new LinkedHashMap<Token,Object>();
        if (!check(RIGHT_PAREN)) {
            do {
                Token param = consume(ID, "Expected parameter name.");
                Object defalt = null;
                if (match(EQUAL)) {
                    defalt = expression();
                }
                params.put(param,defalt);
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expected ')' after function parameters.");
        consume(LEFT_BRACE, "Expected '{' for block after function parameters.");
        Stmt body = block();
        return new Stmt.Function(name, params, body);
    }

    private Stmt classStmt() {
        Token name = consume(ID, "Expected class name.");
        Expr.Var inherits = null;
        if (match(COLON)) {
            inherits = new Expr.Var(consume(ID, "Expected name of inherited class."));
        }
        consume(LEFT_BRACE, "Expected '{' after class name.");
        List<Stmt> mthds = new ArrayList<Stmt>();
        while (!check(RIGHT_BRACE)) {
            Token mthdName = consume(ID, "Expected method name.");
            consume(LEFT_PAREN, "Expected '(' after method name.");
            LinkedHashMap<Token,Object> params = new LinkedHashMap<Token,Object>();
            if (!check(RIGHT_PAREN)) {
                do {
                    Token param = consume(ID, "Expected parameter name.");
                    Object defalt = null;
                    if (match(EQUAL)) {
                        defalt = expression();
                    }
                    params.put(param,defalt);
                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expected ')' after method parameters.");
            consume(LEFT_BRACE, "Expected '{' for block after method parameters.");
            Stmt body = block();
            mthds.add(new Stmt.Method(mthdName, params, body));
        }
        consume(RIGHT_BRACE, "Expected '}' after class body.");
        // Same as block, eat up EOSs that could break latter code.
        eatNewlines();
        return new Stmt.Class(name, inherits, mthds);
    }

    private Stmt importStmt() {
        Token modName = consume(ID, "Expected module name.");
        Token from = null;
        if (match(FROM)) {
            from = consume(STR, "Expected string to import from. (path or URI)");
        }
        consume(EOS, "Expected newline or semicolon after import statement.");
        return new Stmt.Import(modName, from);
    }
    
    private Expr expression() {
        return assign();
    }

    private Expr assign() {
        /* The reason we don't desugar x+=1 to x=x+1 is because
        as in Python, we may want to have different functionality for +=
        than we have for + (consider a basic object that just holds an int.
        if we want to add two together, we might create a new object with
        the sum of our first two, but if we're using +=, we might just update
        in place to reduce the number of objects created). */
        Expr expr = or();
        if (match(EQUAL,PLUS_EQUAL,MINUS_EQUAL,STAR_EQUAL,SLASH_EQUAL,STAR_STAR_EQUAL,PERCENT_EQUAL)) {
            Token equals = previous();
            Expr value = assign();
            if (expr instanceof Expr.Var) {
                Token name = ((Expr.Var)expr).name;
                return new Expr.Assign(name, equals, value);
            } else if (expr instanceof Expr.Get) {
                Expr thing = ((Expr.Get)expr).expr;
                Token name = ((Expr.Get)expr).name;
                return new Expr.Set(thing, name, equals, value);
            } else if (expr instanceof Expr.Index) {
                Expr thing = ((Expr.Index)expr).expr;
                Expr index = ((Expr.Index)expr).index;
                return new Expr.SetIndex(thing, index, equals, value);
            }
            error(equals, "Invalid assignment target."); 
        } else if (match(PLUS_PLUS,MINUS_MINUS)) {
            Token equals = previous();
            Expr value = null;
            if (expr instanceof Expr.Var) {
                Token name = ((Expr.Var)expr).name;
                return new Expr.Assign(name, equals, value);
            } else if (expr instanceof Expr.Get) {
                Expr thing = ((Expr.Get)expr).expr;
                Token name = ((Expr.Get)expr).name;
                return new Expr.Set(thing, name, equals, value);
            }
            error(equals, "Invalid target for '" + equals.lexeme + "'.");
        }
        return expr;
    }

    private Expr or() {
        Expr expr = and();
        while (match(OR, XOR)) {
            Token op = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, op, right);
        }
        return expr;
    }

    private Expr and() {
        Expr expr = not();
        while (match(AND)) {
            Token op = previous();
            Expr right = not();
            expr = new Expr.Logical(expr, op, right);
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
                Token paren = previous();
                List<Expr> args = new ArrayList<Expr>();
                if (!check(RIGHT_PAREN)) {
                    args.add(expression());
                    while (match(COMMA)) {
                        args.add(expression());
                    }
                }
                consume(RIGHT_PAREN, "Expected ')' after function call.");
                expr = new Expr.Call(expr, paren, args);
            } else if (previous().type == DOT) {
                Token name = consume(ID, "Expected attribute or method name after '.'.");
                expr = new Expr.Get(expr, name);
            } else {
                Expr index = expression();
                consume(RIGHT_SQUARE, "Expected closing ']' after index.");
                expr = new Expr.Index(expr, index);
            }
        }
        return expr;
    }

    private Expr primary() {
        if (match(TRUE)) return new Expr.Literal(new Value.BluejayBoolean(true));
        if (match(FALSE)) return new Expr.Literal(new Value.BluejayBoolean(false));
        if (match(NULL)) return new Expr.Literal(new Value.Null());
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
            List<Expr> elems = new ArrayList<Expr>();
            if (!check(RIGHT_SQUARE)) {
                elems.add(expression());
                while (match(COMMA)) {
                    elems.add(expression());
                }
            }
            consume(RIGHT_SQUARE, "Expected ',' or ']' after list elements.");
            return new Expr.ListLiteral(elems);
        }
        if (match(LEFT_BRACE)) {
            List<Expr> keys = new ArrayList<Expr>();
            List<Expr> vals = new ArrayList<Expr>();
            if (!check(RIGHT_BRACE)) {
                do {
                    keys.add(expression());
                    consume(COLON, "Expected ':' after dictionary key.");
                    vals.add(expression());
                } while (match(COMMA));
            }
            consume(RIGHT_BRACE, "Expected ',' or '}' after dictionary elements.");
            return new Expr.Dict(keys, vals);
        }
        throw error(peek(), "Expression expected.");
    }

    @SuppressWarnings("incomplete-switch") // DEBUG: leave until body is complete 
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
            if (previous().type == EOS && tokens.get(current - 2).type != RIGHT_BRACE) return;
            switch (peek().type) {
                // TODO: add cases for every token we know starts a statement (IF, WHILE, etc.)
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

    @SuppressWarnings("unused")
    private boolean checkNext(TokenType... types) {
        return checkNext(1, types);
    }

    private boolean checkNext(int amount, TokenType... types) {
        if (isAtEnd()) return false;
        return Arrays.asList(types).contains(peekNext(amount).type);
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF; //|| ((peek().type == EOS) && (peek().lexeme == "") && (peekNext().type == EOS));
    }

    private Token peek() {
        return tokens.get(current);
    }

    @SuppressWarnings("unused")
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

    private Token consumeSemicolon() {
        if (check(EOS) && peek().lexeme.contains(";")) return advance();
        throw error(peek(), "Expected ';'.");
    }

    private boolean checkSemicolon() {
        if (isAtEnd()) return false;
        return check(EOS) && peek().lexeme.contains(";");
    }

    private boolean matchSemicolon() {
        if (check(EOS) && peek().lexeme.contains(";")) {
            advance();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean matchNewline() {
        if (check(EOS) && peek().lexeme.contains(System.getProperty("line.separator"))) {
            advance();
            return true;
        }
        return false;
    }

    private void eatNewlines() {
        while (check(EOS) && peek().lexeme.contains(System.getProperty("line.separator"))) {
            advance();
        }
    }

    private ParseError error(Token token, String message) {
        // Both disply the error and force the parser into panic mode.
        Bluejay.error(token, message);
        return new ParseError();
    }
}