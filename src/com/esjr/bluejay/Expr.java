package com.esjr.bluejay;

import java.util.*;

abstract class Expr {
    public interface Visitor<T> {
        T visit(Assign expr);
        T visit(Binary expr);
        T visit(Call expr);
        T visit(Grouping expr);
        T visit(Literal expr);
        T visit(Logical expr);
        T visit(Unary expr);
        T visit(Variable expr);
    }

    static class Assign extends Expr {
        Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
        public final Expr value;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator_, Expr right) {
            this.left = left;
            this.operator_ = operator_;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr left;
        public final Token operator_;
        public final Expr right;
    }

    static class Call extends Expr {
        Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr expression;
    }

    static class Literal extends Expr {
        Literal(object value) {
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final object value;
    }

    static class Logical extends Expr {
        Logical(Expr left, Token operator_, Expr right) {
            this.left = left;
            this.operator_ = operator_;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr left;
        public final Token operator_;
        public final Expr right;
    }

    static class Unary extends Expr {
        Unary(Token operator_, Expr right) {
            this.operator_ = operator_;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token operator_;
        public final Expr right;
    }

    static class Variable extends Expr {
        Variable(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
    }

    abstract T accept<T>(Visitor<T> visitor);
}