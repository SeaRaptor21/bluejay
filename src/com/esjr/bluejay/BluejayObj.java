package com.esjr.bluejay;

import java.util.*;

class BluejayObj extends Value {
    public final BluejayClass class_;
    public final Environment attributes = new Environment();
    public final Map<String, Object> specialAttrs = new HashMap<>();
    BluejayObj(BluejayClass class_) {
        this.class_ = class_;
    }

    public Value getAttr(Interpreter i, Token attr) {
        Value res;
        if (attributes.values.containsKey(attr.lexeme)) res = attributes.values.get(attr.lexeme);
        else res = class_.getStatic(attr.lexeme);
        if (res == null) throw new RuntimeError.AttributeError(attr, "Undefined attribute '"+attr.lexeme+"' of "+toString());
        if (res instanceof BluejayMethod) {
            return ((BluejayMethod)res).register(this);
        }
        if (res instanceof NativeMethod) {
            return ((NativeMethod)res).register(this);
        }
        return res;
    }

    public void setAttr(Interpreter i, String attr, Token operator, Value v) {
        if (attributes.values.containsKey(attr)) {
            attributes.assignStr(i, attr, operator, v);
        } else {
            if (operator.type != TokenType.EQUAL)
                throw new RuntimeError.AttributeError(operator, "Cannot assign to undefined attribute '"+attr+"' of "+toString());
            attributes.define(attr, v);
        }
    }

    public String toString(Interpreter i) {
        Value maybeStr = class_.getStatic("$str");
        if (maybeStr != null) {
            if (maybeStr instanceof BluejayMethod) {
                Value res = ((BluejayMethod)maybeStr).call(i, new ArrayList<Value>(), this);
                if (!(res instanceof Value.BluejayString)) throw new RuntimeError.TypeError("The '$str' method must return a string.");
                else return ((Value.BluejayString)res).value;
            } else if (maybeStr instanceof NativeMethod) {
                Value res = ((NativeMethod)maybeStr).call(i, new ArrayList<Value>(), this);
                if (!(res instanceof Value.BluejayString)) throw new RuntimeError.TypeError("The '$str' method must return a string.");
                else return ((Value.BluejayString)res).value;
            } else {
                throw new RuntimeError.TypeError("The attribute name '$str' is reserved for the initializer of "+class_.name);
            }
        }
        return "<" + class_.name + " object>";
    }
    
    public Value add(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '+' for object");
    }
    public Value sub(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '-' for object");
    }
    public Value mul(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '*' for object");
    }
    public Value div(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '/' for object");
    }
    public Value mod(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '%' for object");
    }
    public Value pow(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '**' for object");
    }
    public Value eq(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '==' for object");
    }
    public Value lt(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '<' for object");
    }
    public Value lte(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '<=' for object");
    }
    public Value gt(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '>' for object");
    }
    public Value gte(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '>=' for object");
    }
    public Value ne(Interpreter i, Value other) {
        throw new RuntimeError("Unsupported operation '!=' for object");
    }
    public Value neg(Interpreter i) {
        throw new RuntimeError("Unsupported operation unary '-' for object");
    }
    public Value uadd(Interpreter i) {
        throw new RuntimeError("Unsupported operation unary '+' for object");
    }
    public Value getItem(Interpreter i, Value index) {
        throw new RuntimeError("Cannot index on object");
    }
    public Value setItem(Interpreter i, Value index, Value v) {
        throw new RuntimeError("Cannot index on object");
    }
}