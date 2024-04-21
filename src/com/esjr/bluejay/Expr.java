package com.esjr.bluejay;

import java.util.*;

abstract class Expr {
    public interface Visitor<T> {
        T visit(Assign expr);
        T visit(Attr expr);
        T visit(Binary expr);
        T visit(Call expr);
        T visit(Grouping expr);
        T visit(Literal expr);
        T visit(Logical expr);
        T visit(Unary expr);
        T visit(Var expr);
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

    static class Attr extends Expr {
        Attr(Expr expr, Token name) {
            this.expr = expr;
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr expr;
        public final Token name;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    static class Call extends Expr {
        Call(Expr callee, List<Expr> arguments) {
            this.callee = callee;
            this.arguments = arguments;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Expr callee;
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
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Object value;
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
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token operator;
        public final Expr right;
    }

    static class Var extends Expr {
        Var(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visit(this);
        }

        public final Token name;
    }

    abstract <T> T accept(Visitor<T> visitor);
}