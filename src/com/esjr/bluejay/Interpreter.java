package com.esjr.bluejay;

import java.util.*;
import static com.esjr.bluejay.TokenType.*;

class Interpreter implements Expr.Visitor<Value>, Stmt.Visitor<Object> {
    final Environment globals = Builtins.globals;
    public Environment environment = globals;
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
        if (v instanceof BluejayObj && ((BluejayObj)v).class_ == Builtins.numberClass)
            v = new Value.Number(v.toNumber(this));
        if (!(v instanceof Value.Number) || ((Value.Number)v).value%1 != 0) throw new RuntimeError(stmt.keyword, "Break amount must be an integer.");
        throw new Break((int)((Value.Number)v).value, stmt);
    }

    public Void visit(Stmt.Class stmt) {
        Value inherits = stmt.inherits != null ? eval(stmt.inherits) : null;
        if (!(inherits instanceof BluejayClass) && inherits != null) throw new RuntimeError(stmt.inherits.name, "A class can only inherit from another classes.");
        BluejayClass class_ = new BluejayClass(stmt, (BluejayClass)inherits);
        environment.define(stmt.name.lexeme, class_);
        return null;
    }

    public Void visit(Stmt.Expression stmt) {
        eval(stmt.expression);
        return null;
    }

    public Void visit(Stmt.Foreach stmt) {
        Value iter = eval(stmt.iter);
        if (!(iter instanceof BluejayObj)) throw new RuntimeError(stmt.loopVar, "Cannot iterate over object of type '"+typeOf(iter)+"'.");
        while (((BluejayObj)iter).class_ != Builtins.listClass) {
            if (!(iter instanceof BluejayObj)) throw new RuntimeError(stmt.loopVar, "Cannot iterate over object of type '"+typeOf(iter)+"'.");
            Value iterMthd = ((BluejayObj)iter).class_.getStatic("$iter");
            if (iterMthd instanceof BluejayMethod && iterMthd != null) {
                iter = ((BluejayRegisteredMethod)((BluejayMethod)iterMthd).register((BluejayObj)iter)).call(this, new ArrayList<>());
            } else {
                throw new RuntimeError(stmt.loopVar, "Cannot iterate over object of type '"+typeOf(iter)+"'.");
            }
        }
        Environment previous = environment;
        try {
            environment = new Environment(environment);
            environment.define(stmt.loopVar.lexeme, new Value.Null());
            @SuppressWarnings("unchecked")
            List<Value> iterable = (List<Value>)((BluejayObj)iter).specialAttrs.get("elements");
            for (Value v : iterable) {
                environment.assign(this, stmt.loopVar, new Token(EQUAL, "="), v);
                try {
                    exec(stmt.body);
                } catch (Break b) {
                    if (b.amount == 1) break;
                    else throw new Break(b.amount-1, b.statement);
                }
            }
        } finally {
            environment = previous;
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
        if (amount instanceof BluejayObj && ((BluejayObj)amount).class_ == Builtins.numberClass) {
            double num = ((BluejayObj)amount).toNumber(this);
            for (int i=0; i<num; i++) {
                try {
                    exec(stmt.body);
                } catch (Break b) {
                    if (b.amount == 1) break;
                    else throw new Break(b.amount-1, b.statement);
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
                if (b.amount == 1) break;
                else throw new Break(b.amount-1, b.statement);
            }
            cond = eval(stmt.condition);
        }
        return null;
    }

    public Value visit(Expr.Assign expr) {
        Value v = (expr.value != null) ? eval(expr.value) : null;
        assignVariable(expr.name, expr, expr.operator, v);
        return v;
    }

    public Value visit(Expr.Get expr) {
        return eval(expr.expr).getAttr(this, expr.name);
    }

    public Value visit(Expr.Set expr) {
        Value obj = eval(expr.expr);
        Value v = (expr.value != null) ? eval(expr.value) : null;
        obj.setAttr(this, expr.name.lexeme, expr.operator, v);
        return v;
    }

    public Value visit(Expr.Binary expr) {
        Value left = eval(expr.left);
        Value right = eval(expr.right);
        switch (expr.operator.type) {
            case PLUS: return left.add(this, right);
            case MINUS: return left.sub(this, right);
            case STAR: return left.mul(this, right);
            case SLASH: return left.div(this, right);
            case PERCENT: return left.mod(this, right);
            case STAR_STAR: return left.pow(this, right);
            case EQUAL_EQUAL: return left.eq(this, right);
            case BANG_EQUAL: return left.ne(this, right);
            case LESS: return left.lt(this, right);
            case LESS_EQUAL: return left.lte(this, right);
            case GREATER: return left.gt(this, right);
            case GREATER_EQUAL: return left.gte(this, right);
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
        return eval(expr.expr).getItem(this, eval(expr.index));
    }

    public Value visit(Expr.SetIndex expr) {
        Value value = eval(expr.value);
        if (expr.operator.type == EQUAL) {
            eval(expr.expr).setItem(this, eval(expr.index), value);
            return value;
        } else if (expr.operator.type == PLUS_EQUAL) {
            Value obj = eval(expr.expr);
            Value i = eval(expr.index);
            value = obj.getItem(this, i).add(this, value);
            obj.setItem(this, i, value);
            return value;
        } else if (expr.operator.type == MINUS_EQUAL) {
            Value obj = eval(expr.expr);
            Value i = eval(expr.index);
            value = obj.getItem(this, i).sub(this, value);
            obj.setItem(this, i, value);
            return value;
        } else if (expr.operator.type == STAR_EQUAL) {
            Value obj = eval(expr.expr);
            Value i = eval(expr.index);
            value = obj.getItem(this, i).mul(this, value);
            obj.setItem(this, i, value);
            return value;
        } else if (expr.operator.type == SLASH_EQUAL) {
            Value obj = eval(expr.expr);
            Value i = eval(expr.index);
            value = obj.getItem(this, i).div(this, value);
            obj.setItem(this, i, value);
            return value;
        } else if (expr.operator.type == PERCENT_EQUAL) {
            Value obj = eval(expr.expr);
            Value i = eval(expr.index);
            value = obj.getItem(this, i).mod(this, value);
            obj.setItem(this, i, value);
            return value;
        } else if (expr.operator.type == STAR_STAR_EQUAL) {
            Value obj = eval(expr.expr);
            Value i = eval(expr.index);
            value = obj.getItem(this, i).pow(this, value);
            obj.setItem(this, i, value);
            return value;
        }
        throw new UnsupportedOperationException("Token "+expr.operator.lexeme+" is not a valid operator.");
    }

    public Value visit(Expr.ListLiteral expr) {
        BluejayObj list = (BluejayObj)Builtins.listClass.call(this, new ArrayList<Value>());
        try {
            @SuppressWarnings("unchecked")
            List<Value> elems = (List<Value>)list.specialAttrs.get("elements");
            for (Expr e : expr.elements) {
                elems.add(eval(e));
            }
        } catch (ClassCastException e) {
            System.err.println("Cannot access elements of list.");
        }
        return list;
    }

    public Value visit(Expr.Literal expr) {
        if (expr.value instanceof Value.BluejayString) {
            List<Value> args = new ArrayList<>();
            args.add(expr.value);
            return (BluejayObj)Builtins.stringClass.call(this, args);
        } else if (expr.value instanceof Value.Number) {
            List<Value> args = new ArrayList<>();
            args.add(expr.value);
            return (BluejayObj)Builtins.numberClass.call(this, args);
        } else {
            return expr.value;
        }
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
                return new Value.BluejayBoolean(truthy(left) != truthy(right));
            default:
                throw new UnsupportedOperationException("Token "+expr.operator.lexeme+" is not a valid operator.");
        }
    }

    public Value visit(Expr.Unary expr) {
        Value right = eval(expr.right);
        switch (expr.operator.type) {
            case MINUS:
                return right.neg(this);
            case PLUS:
                return right.uadd(this);
            case BANG:
            case NOT:
                return new Value.BluejayBoolean(!truthy(right));
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
        //if (name.type == THIS) return environment.get(name);
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    private void assignVariable(Token name, Expr expr, Token operator, Value value) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(this, distance, name.lexeme, operator, value);
        } else {
            globals.assign(this, name, operator, value);
        }
    }

    public String typeOf(Value x) {
        return x.getClass().getSimpleName();
    }

    public boolean truthy(Value x) {
        if (x instanceof Value.BluejayBoolean) {
            return ((Value.BluejayBoolean)x).value;
        } else if (x instanceof Value.Number) {
            return ((Value.Number)x).value != 0;
        } else if (x instanceof Value.BluejayString) {
            return ((Value.BluejayString)x).value.length() > 0;
        } else if (x instanceof Value.BluejayList) {
            return ((Value.BluejayList)x).elements.size() > 0;
        } else if (x instanceof Value.Dict) {
            return ((Value.Dict)x).elements.size() > 0;
        } else if (x instanceof Value.Null) {
            return false;
        } else if (x instanceof BluejayObj) {
            return ((BluejayObj)x).toBoolean(this);
        }
        return false;
    }
}