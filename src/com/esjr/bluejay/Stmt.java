package com.esjr.bluejay;

import java.util.*;

abstract class Stmt {
    public interface Visitor<T> {
        T visit(Block stmt);
        T visit(Break stmt);
        T visit(Expression stmt);
        T visit(Function stmt);
        T visit(If stmt);
        T visit(Print stmt);
        T visit(Return stmt);
        T visit(Var stmt);
        T visit(While stmt);
    }

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final List<Stmt> statements;
    }

    static class Break extends Stmt {
        Break(Token token, Expr value) {
            this.token = token;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token token;
        public final Expr value;
    }

    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr expression;
    }

    static class Function extends Stmt {
        Function(Token name, List<Token> parameters, List<Stmt> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final List<Token> parameters;
        public final List<Stmt> body;
    }

    static class If extends Stmt {
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr expression;
    }

    static class Return extends Stmt {
        Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token keyword;
        public final Expr value;
    }

    static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final Expr initializer;
    }

    static class While extends Stmt {
        While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr condition;
        public final Stmt body;
    }

    abstract T accept<T>(Visitor<T> visitor);
}