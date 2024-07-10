package com.esjr.bluejay;

import java.util.*;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    public String visit(Object thing) {
        if (thing instanceof Expr) return visit((Expr)thing);
        if (thing instanceof Stmt) return visit((Stmt)thing);
        return thing.toString();
    }

    public String visit(List<Object> thing) {
        List<String> elems = new ArrayList<String>();
        for (Object e : thing) {
            elems.add(visit(e));
        }
        return "["+String.join(", ", elems)+"]";
    }

    public String visit(Expr thing) {
        if (thing instanceof Expr.Assign) return visit((Expr.Assign)thing);
        if (thing instanceof Expr.Get) return visit((Expr.Get)thing);
        if (thing instanceof Expr.Set) return visit((Expr.Set)thing);
        if (thing instanceof Expr.Binary) return visit((Expr.Binary)thing);
        if (thing instanceof Expr.Call) return visit((Expr.Call)thing);
        if (thing instanceof Expr.Dict) return visit((Expr.Dict)thing);
        if (thing instanceof Expr.Grouping) return visit((Expr.Grouping)thing);
        if (thing instanceof Expr.Index) return visit((Expr.Index)thing);
        if (thing instanceof Expr.SetIndex) return visit((Expr.SetIndex)thing);
        if (thing instanceof Expr.ListLiteral) return visit((Expr.ListLiteral)thing);
        if (thing instanceof Expr.Literal) return visit((Expr.Literal)thing);
        if (thing instanceof Expr.Logical) return visit((Expr.Logical)thing);
        if (thing instanceof Expr.Unary) return visit((Expr.Unary)thing);
        if (thing instanceof Expr.Var) return visit((Expr.Var)thing);
        return null;
    }

    public String visit(Expr.Assign thing) {
        return "Expr.Assign: " + "NAME(" + visit(thing.name) + "), " + "OPERATOR(" + visit(thing.operator) + "), " + "VALUE(" + visit(thing.value) + ");";
    }

    public String visit(Expr.Get thing) {
        return "Expr.Get: " + "EXPR(" + visit(thing.expr) + "), " + "NAME(" + visit(thing.name) + ");";
    }

    public String visit(Expr.Set thing) {
        return "Expr.Set: " + "EXPR(" + visit(thing.expr) + "), " + "NAME(" + visit(thing.name) + "), " + "OPERATOR(" + visit(thing.operator) + "), " + "VALUE(" + visit(thing.value) + ");";
    }

    public String visit(Expr.Binary thing) {
        return "Expr.Binary: " + "LEFT(" + visit(thing.left) + "), " + "OPERATOR(" + visit(thing.operator) + "), " + "RIGHT(" + visit(thing.right) + ");";
    }

    public String visit(Expr.Call thing) {
        return "Expr.Call: " + "CALLEE(" + visit(thing.callee) + "), " + "PAREN(" + visit(thing.paren) + "), " + "ARGUMENTS(" + visit(thing.arguments) + ");";
    }

    public String visit(Expr.Dict thing) {
        return "Expr.Dict: " + "KEYS(" + visit(thing.keys) + "), " + "VALUES(" + visit(thing.values) + ");";
    }

    public String visit(Expr.Grouping thing) {
        return "Expr.Grouping: " + "EXPRESSION(" + visit(thing.expression) + ");";
    }

    public String visit(Expr.Index thing) {
        return "Expr.Index: " + "EXPR(" + visit(thing.expr) + "), " + "INDEX(" + visit(thing.index) + ");";
    }

    public String visit(Expr.SetIndex thing) {
        return "Expr.SetIndex: " + "EXPR(" + visit(thing.expr) + "), " + "INDEX(" + visit(thing.index) + "), " + "OPERATOR(" + visit(thing.operator) + "), " + "VALUE(" + visit(thing.value) + ");";
    }

    public String visit(Expr.ListLiteral thing) {
        return "Expr.ListLiteral: " + "ELEMENTS(" + visit(thing.elements) + ");";
    }

    public String visit(Expr.Literal thing) {
        return "Expr.Literal: " + "VALUE(" + visit(thing.value) + ");";
    }

    public String visit(Expr.Logical thing) {
        return "Expr.Logical: " + "LEFT(" + visit(thing.left) + "), " + "OPERATOR(" + visit(thing.operator) + "), " + "RIGHT(" + visit(thing.right) + ");";
    }

    public String visit(Expr.Unary thing) {
        return "Expr.Unary: " + "OPERATOR(" + visit(thing.operator) + "), " + "RIGHT(" + visit(thing.right) + ");";
    }

    public String visit(Expr.Var thing) {
        return "Expr.Var: " + "NAME(" + visit(thing.name) + ");";
    }

    public String visit(Stmt thing) {
        if (thing instanceof Stmt.Block) return visit((Stmt.Block)thing);
        if (thing instanceof Stmt.Break) return visit((Stmt.Break)thing);
        if (thing instanceof Stmt.Class) return visit((Stmt.Class)thing);
        if (thing instanceof Stmt.Expression) return visit((Stmt.Expression)thing);
        if (thing instanceof Stmt.Foreach) return visit((Stmt.Foreach)thing);
        if (thing instanceof Stmt.Function) return visit((Stmt.Function)thing);
        if (thing instanceof Stmt.If) return visit((Stmt.If)thing);
        if (thing instanceof Stmt.Import) return visit((Stmt.Import)thing);
        if (thing instanceof Stmt.Method) return visit((Stmt.Method)thing);
        if (thing instanceof Stmt.Print) return visit((Stmt.Print)thing);
        if (thing instanceof Stmt.Repeat) return visit((Stmt.Repeat)thing);
        if (thing instanceof Stmt.Return) return visit((Stmt.Return)thing);
        if (thing instanceof Stmt.Var) return visit((Stmt.Var)thing);
        if (thing instanceof Stmt.While) return visit((Stmt.While)thing);
        return null;
    }

    public String visit(Stmt.Block thing) {
        return "Stmt.Block: " + "STATEMENTS(" + visit(thing.statements) + ");";
    }

    public String visit(Stmt.Break thing) {
        return "Stmt.Break: " + "KEYWORD(" + visit(thing.keyword) + "), " + "VALUE(" + visit(thing.value) + ");";
    }

    public String visit(Stmt.Class thing) {
        return "Stmt.Class: " + "NAME(" + visit(thing.name) + "), " + "INHERITS(" + visit(thing.inherits) + "), " + "METHODS(" + visit(thing.methods) + ");";
    }

    public String visit(Stmt.Expression thing) {
        return "Stmt.Expression: " + "EXPRESSION(" + visit(thing.expression) + ");";
    }

    public String visit(Stmt.Foreach thing) {
        return "Stmt.Foreach: " + "LOOPVAR(" + visit(thing.loopVar) + "), " + "ITER(" + visit(thing.iter) + "), " + "BODY(" + visit(thing.body) + ");";
    }

    public String visit(Stmt.Function thing) {
        return "Stmt.Function: " + "NAME(" + visit(thing.name) + "), " + "PARAMETERS(" + visit(thing.parameters) + "), " + "BODY(" + visit(thing.body) + ");";
    }

    public String visit(Stmt.If thing) {
        return "Stmt.If: " + "CONDITION(" + visit(thing.condition) + "), " + "THENBRANCH(" + visit(thing.thenBranch) + "), " + "ELSEBRANCH(" + visit(thing.elseBranch) + ");";
    }

    public String visit(Stmt.Import thing) {
        return "Stmt.Import: " + "NAME(" + visit(thing.name) + "), " + "FROM(" + visit(thing.from) + ");";
    }

    public String visit(Stmt.Method thing) {
        return "Stmt.Method: " + "NAME(" + visit(thing.name) + "), " + "PARAMETERS(" + visit(thing.parameters) + "), " + "BODY(" + visit(thing.body) + ");";
    }

    public String visit(Stmt.Print thing) {
        return "Stmt.Print: " + "EXPRESSION(" + visit(thing.expression) + ");";
    }

    public String visit(Stmt.Repeat thing) {
        return "Stmt.Repeat: " + "PAREN(" + visit(thing.paren) + "), " + "AMOUNT(" + visit(thing.amount) + "), " + "BODY(" + visit(thing.body) + ");";
    }

    public String visit(Stmt.Return thing) {
        return "Stmt.Return: " + "KEYWORD(" + visit(thing.keyword) + "), " + "VALUE(" + visit(thing.value) + ");";
    }

    public String visit(Stmt.Var thing) {
        return "Stmt.Var: " + "NAME(" + visit(thing.name) + "), " + "INITIALIZER(" + visit(thing.initializer) + ");";
    }

    public String visit(Stmt.While thing) {
        return "Stmt.While: " + "CONDITION(" + visit(thing.condition) + "), " + "BODY(" + visit(thing.body) + ");";
    }
}