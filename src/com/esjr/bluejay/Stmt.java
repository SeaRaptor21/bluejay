package com.esjr.bluejay;

import java.util.*;

abstract class Stmt {
    public interface Visitor<T> {
        T visit(Block stmt);
        T visit(Break stmt);
        T visit(Class stmt);
        T visit(Expression stmt);
        T visit(Foreach stmt);
        T visit(Function stmt);
        T visit(If stmt);
        T visit(Import stmt);
        T visit(Method stmt);
        T visit(Print stmt);
        T visit(Repeat stmt);
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
        Break(Token keyword, Expr value) {
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

    static class Class extends Stmt {
        Class(Token name, Expr.Var inherits, List<Stmt> methods) {
            this.name = name;
            this.inherits = inherits;
            this.methods = methods;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final Expr.Var inherits;
        public final List<Stmt> methods;
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

    static class Foreach extends Stmt {
        Foreach(Token loopVar, Expr iter, Stmt body) {
            this.loopVar = loopVar;
            this.iter = iter;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token loopVar;
        public final Expr iter;
        public final Stmt body;
    }

    static class Function extends Stmt {
        Function(Token name, LinkedHashMap<Token,Object> parameters, Stmt body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final LinkedHashMap<Token,Object> parameters;
        public final Stmt body;
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

    static class Import extends Stmt {
        Import(Token name, Token from) {
            this.name = name;
            this.from = from;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final Token from;
    }

    static class Method extends Stmt {
        Method(Token name, LinkedHashMap<Token,Object> parameters, Stmt body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final LinkedHashMap<Token,Object> parameters;
        public final Stmt body;
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

    static class Repeat extends Stmt {
        Repeat(Token paren, Expr amount, Stmt body) {
            this.paren = paren;
            this.amount = amount;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token paren;
        public final Expr amount;
        public final Stmt body;
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

    abstract <T> T accept(Visitor<T> visitor);
}