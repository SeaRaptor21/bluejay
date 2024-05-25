package com.esjr.bluejay;

import java.util.*;
import static com.esjr.bluejay.TokenType.*;

class Interpreter implements Expr.Visitor<Value>, Stmt.Visitor<Object> {
    final Environment globals = Builtins.globals;
    private Environment environment = globals;
    private final Map<Expr, Integer> locals = new HashMap<>();

    public static class Break extends RuntimeException {
        int amount;
        Stmt.Break statement;
        Break(int amount, Stmt.Break statement) {
            this.amount = amount;
            this.statement = statement;
        }
    }

    public static class Return extends RuntimeException {
        Value value;
        Stmt.Return statement;
        Return(Value value, Stmt.Return statement) {
            this.value = value;
            this.statement = statement;
        }
    }
    
    void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                try {
                    exec(stmt);
                } catch (Break b) {
                    throw new RuntimeError(b.statement.keyword, "Break outside of loop.");
                } catch (Return r) {
                    throw new RuntimeError(r.statement.keyword, "Return outside of function or method.");
                }
            }
        } catch (RuntimeError error) {
            Bluejay.runtimeError(error);
        }
    }
    
    public void exec(List<Stmt> stmts) {
        for (Stmt stmt : stmts) {
            exec(stmt);
        }
    }

    private void exec(Stmt stmt) {
        stmt.accept(this);
    }

    private Value eval(Expr expr) {
        return expr.accept(this);
    }

    public Void visit(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    public Void visit(Stmt.Break stmt) {
        Value v = stmt.value == null ? new Value.Number(1) : eval(stmt.value);
        if (!(v instanceof Value.Number) || ((Value.Number)v).value%0 != 0) throw new RuntimeError(stmt.keyword, "Break amount must be an integer.");
        throw new Break((int)((Value.Number)v).value, stmt);
    }

    public Void visit(Stmt.Class stmt) {
        BluejayClass klass = new BluejayClass(stmt);
        environment.define(stmt.name.lexeme, klass);
        return null;
    }

    public Void visit(Stmt.Expression stmt) {
        eval(stmt.expression);
        return null;
    }

    public Void visit(Stmt.Foreach stmt) {
        Value iter = eval(stmt.iter);
        if (iter instanceof BluejayIterator) {
            Environment previous = environment;
            try {
                environment = new Environment(environment);
                environment.define(stmt.loopVar.lexeme, new Value.Null());
                for (Value v : ((BluejayIterator)iter).iter()) {
                    environment.assign(stmt.loopVar, v);
                    try {
                        exec(stmt.body);
                    } catch (Break b) {
                        break;
                    }
                }
            } finally {
                environment = previous;
            }
        } else {
            throw new RuntimeError(stmt.loopVar, "Cannot iterate over object of type '"+typeOf(iter)+"'.");
        }
        return null;
    }

    public Void visit(Stmt.Function stmt) {
        BluejayFunction function = new BluejayFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    public Void visit(Stmt.If stmt) {
        Value cond = eval(stmt.condition);
        if (truthy(cond)) {
            exec(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            exec(stmt.elseBranch);
        }
        return null;
    }

    public Void visit(Stmt.Import stmt) {
        throw new UnsupportedOperationException("Imports not implemented yet.");
    }

    public Void visit(Stmt.Method stmt) {
        throw new UnsupportedOperationException("Stmt.Method should never be directly run by the interpreter.");
    }

    public Void visit(Stmt.Print stmt) {
        throw new UnsupportedOperationException("Stmt.Print has been deprecated.");
    }

    public Void visit(Stmt.Repeat stmt) {
        Value amount = eval(stmt.amount);
        if (amount instanceof Value.Number) {
            for (int i=0; i<((Value.Number)amount).value; i++) {
                try {
                    exec(stmt.body);
                } catch (Break b) {
                    break;
                }
            }
        } else {
            throw new RuntimeError(stmt.paren, "Amount for 'repeat' statement must be a number.");
        }
        return null;
    }

    public Void visit(Stmt.Return stmt) {
        Value v = stmt.value == null ? new Value.Null() : eval(stmt.value);
        throw new Return(v, stmt);
    }

    public Void visit(Stmt.Var stmt) {
        environment.define(stmt.name.lexeme, stmt.initializer == null ? new Value.Null() : eval(stmt.initializer));
        return null;
    }

    public Void visit(Stmt.While stmt) {
        Value cond = eval(stmt.condition);
        while (truthy(cond)) {
            try {
                exec(stmt.body);
            } catch (Break b) {
                break;
            }
            cond = eval(stmt.condition);
        }
        return null;
    }

    public Value visit(Expr.Assign expr) {
        if (expr.operator.type != TokenType.EQUAL) throw new UnsupportedOperationException("Assignment operators other than '=' have not been implemented yet.");
        Value v = eval(expr.value);
        assignVariable(expr.name, expr, v);
        return v;
    }

    public Value visit(Expr.Get expr) {
        return eval(expr.expr).getAttr(expr.name.lexeme);
    }

    public Value visit(Expr.Set expr) {
        if (expr.operator.type != EQUAL) throw new UnsupportedOperationException("Assignment operators other than '=' have not been implemented yet.");
        Value obj = eval(expr.expr);
        Value v = eval(expr.value);
        obj.setAttr(expr.name.lexeme, v);
        return v;
    }

    public Value visit(Expr.Binary expr) {
        Value left = eval(expr.left);
        Value right = eval(expr.right);
        switch (expr.operator.type) {
            case PLUS: return left.add(right);
            case MINUS: return left.sub(right);
            case STAR: return left.mul(right);
            case SLASH: return left.div(right);
            case PERCENT: return left.mod(right);
            case STAR_STAR: return left.pow(right);
            case EQUAL_EQUAL: return left.eq(right);
            case BANG_EQUAL: return left.ne(right);
            case LESS: return left.lt(right);
            case LESS_EQUAL: return left.lte(right);
            case GREATER: return left.gt(right);
            case GREATER_EQUAL: return left.gte(right);
            default:
                throw new UnsupportedOperationException("Token "+expr.operator.lexeme+" is not a valid operator.");
        }
    }

    public Value visit(Expr.Call expr) {
        Value callee = eval(expr.callee);
        List<Value> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) { 
            arguments.add(eval(argument));
        }
        if (!(callee instanceof BluejayCallable)) {
            throw new RuntimeError(expr.paren, "Object of type '"+typeOf(callee)+"' is not callable.");
        }
        BluejayCallable function = (BluejayCallable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                function.arity() + " arguments but got " +
                arguments.size() + ".");
        }
        try {
            return function.call(this, arguments);
        } catch (Return returnVal) {
            return returnVal.value;
        }
    }

    public Value visit(Expr.Dict expr) {
        Map<Value,Value> map = new HashMap<>();
        for (int i=0; i<expr.keys.size(); i++) {
            map.put(eval(expr.keys.get(i)), eval(expr.values.get(i)));
        }
        return new Value.Dict(map);
    }

    public Value visit(Expr.Grouping expr) {
        return eval(expr.expression);
    }

    public Value visit(Expr.Index expr) {
        return eval(expr.expr).getItem(eval(expr.index));
    }

    public Value visit(Expr.ListLiteral expr) {
        List<Value> elems = new ArrayList<>();
        for (Expr e : expr.elements) {
            elems.add(eval(e));
        }
        return new Value.List(elems);
    }

    public Value visit(Expr.Literal expr) {
        return expr.value;
    }

    public Value visit(Expr.Logical expr) {
        Value left = eval(expr.left);
        Value right = eval(expr.right);
        switch (expr.operator.type) {
            case AND:
                if (!truthy(left)) return left;
                return right;
            case OR:
                if (truthy(left)) return left;
                return right;
            case XOR:
                return new Value.Boolean(truthy(left) != truthy(right));
            default:
                throw new UnsupportedOperationException("Token "+expr.operator.lexeme+" is not a valid operator.");
        }
    }

    public Value visit(Expr.Unary expr) {
        Value right = eval(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                return right.neg();
            case PLUS:
                return right.uadd();
            case BANG:
            case NOT:
                return new Value.Boolean(!truthy(right));
            default:
                throw new UnsupportedOperationException("Token "+expr.operator.lexeme+" is not a valid operator.");
        }
    }

    public Value visit(Expr.Var expr) {
        return lookUpVariable(expr.name, expr);
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                exec(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    private Value lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void assignVariable(Token name, Expr expr, Value value) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, name.lexeme, value);
        } else {
            globals.assign(name, value);
        }
    }

    private String typeOf(Value x) {
        return "Implement later!"; // TODO: this
    }

    private boolean truthy(Value x) {
        if (x instanceof Value.Boolean) {
            return ((Value.Boolean)x).value;
        } else if (x instanceof Value.Number) {
            return ((Value.Number)x).value != 0;
        } else if (x instanceof Value.String) {
            return ((Value.String)x).value.length() > 0;
        } else if (x instanceof Value.List) {
            return ((Value.List)x).elements.size() > 0;
        } else if (x instanceof Value.Dict) {
            return ((Value.Dict)x).elements.size() > 0;
        } else if (x instanceof Value.Null) {
            return false;
        }
        return false;
    }
}