package com.esjr.bluejay;

import java.util.*;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    public final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private int inClass = 0;
    
    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }
    
    public Void visit(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    public Void visit(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
          resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    public Void visit(Expr.Var expr) {
        if (!scopes.isEmpty() && scopes.peek().containsKey(expr.name.lexeme) && scopes.peek().get(expr.name.lexeme) == false) {
            Bluejay.error(expr.name, "Can't read local variable in its own initializer.");
        }
        if (inClass == 0 && expr.name.type == TokenType.THIS) {
            Bluejay.error(expr.name, "Cannot use 'this' outside of class definition.");
        }
        resolveLocal(expr, expr.name);
        return null;
    }

    public Void visit(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    public Void visit(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }

    public Void visit(Stmt.Break stmt) {
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    public Void visit(Stmt.Class stmt) {
        declare(stmt.name);
        define(stmt.name);

        inClass++;
        
        for (Expr.Var inherit : stmt.inherits) {
            resolveLocal(inherit, inherit.name);
        }
        for (Stmt mthd : stmt.methods) {
            resolve(mthd);
        }

        inClass--;
        return null;
    }
    
    public Void visit(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    public Void visit(Stmt.Foreach stmt) {
        beginScope();
        resolve(stmt.iter);
        declare(stmt.loopVar);
        define(stmt.loopVar);
        resolve(stmt.body);
        endScope();
        return null;
    }

    public Void visit(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    public Void visit(Stmt.Import stmt) {
        declare(stmt.name);
        define(stmt.name);
        return null;
    }

    public Void visit(Stmt.Method stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt);
        return null;
    }

    public Void visit(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    public Void visit(Stmt.Repeat stmt) {
        resolve(stmt.amount);
        resolve(stmt.body);
        return null;
    }

    public Void visit(Stmt.Return stmt) {
        if (stmt.value != null) resolve(stmt.value);
        return null;
    }

    public Void visit(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    public Void visit(Expr.Get expr) {
        return null;
    }

    public Void visit(Expr.Set expr) {
        resolve(expr.value);
        return null;
    }

    public Void visit(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    public Void visit(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr a : expr.arguments) {
            resolve(a);
        }
        return null;
    }

    public Void visit(Expr.Dict expr) {
        for (Expr k : expr.keys) {
            resolve(k);
        }
        for (Expr v : expr.values) {
            resolve(v);
        }
        return null;
    }

    public Void visit(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    public Void visit(Expr.Index expr) {
        resolve(expr.expr);
        resolve(expr.index);
        return null;
    }

    public Void visit(Expr.ListLiteral expr) {
        for (Expr e : expr.elements) {
            resolve(e);
        }
        return null;
    }

    public Void visit(Expr.Literal expr) {
        return null;
    }

    public Void visit(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    public Void visit(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    private void resolveFunction(Stmt.Function function) {
        beginScope();
        for (Token param : function.parameters.keySet()) {
            declare(param);
            define(param);
        }
        resolve(((Stmt.Block)function.body).statements);
        endScope();
    }

    private void resolveFunction(Stmt.Method function) {
        beginScope();
        for (Token param : function.parameters.keySet()) {
            declare(param);
            define(param);
        }
        resolve(((Stmt.Block)function.body).statements);
        endScope();
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i=scopes.size()-1; i>=0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size()-1-i);
                return;
            }
        }
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }
    
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }
    
    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }
}