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

    private Value tryOperator(Interpreter i, String name, Value... arguments) {
        Value maybeOp = class_.getStatic(name);
        if (maybeOp != null) {
            List<Value> args = new ArrayList<>(Arrays.asList(arguments));
            try {
                if (maybeOp instanceof BluejayMethod) {
                    return ((BluejayMethod)maybeOp).call(i, args, this);
                } else if (maybeOp instanceof NativeMethod) {
                    return ((NativeMethod)maybeOp).call(i, args, this);
                } else {
                    throw new RuntimeError.TypeError("The attribute name '"+name+"' of "+class_.name+" should be a method.");
                }
            } catch (Interpreter.Return r) {
                return r.value;
            }
        }
        return null;
    }
    
    public Value add(Interpreter i, Value other) {
        Value res = tryOperator(i, "$add", other);
        if (res == null) throw new RuntimeError("Unsupported operation '+' for object of type "+class_.name);
        return res;
    }
    public Value sub(Interpreter i, Value other) {
        Value res = tryOperator(i, "$sub", other);
        if (res == null) throw new RuntimeError("Unsupported operation '-' for object of type "+class_.name);
        return res;
    }
    public Value mul(Interpreter i, Value other) {
        Value res = tryOperator(i, "$mul", other);
        if (res == null) throw new RuntimeError("Unsupported operation '*' for object of type "+class_.name);
        return res;
    }
    public Value div(Interpreter i, Value other) {
        Value res = tryOperator(i, "$div", other);
        if (res == null) throw new RuntimeError("Unsupported operation '/' for object of type "+class_.name);
        return res;
    }
    public Value mod(Interpreter i, Value other) {
        Value res = tryOperator(i, "$mod", other);
        if (res == null) throw new RuntimeError("Unsupported operation '%' for object of type "+class_.name);
        return res;
    }
    public Value pow(Interpreter i, Value other) {
        Value res = tryOperator(i, "$pow", other);
        if (res == null) throw new RuntimeError("Unsupported operation '**' for object of type "+class_.name);
        return res;
    }
    public Value eq(Interpreter i, Value other) {
        Value res = tryOperator(i, "$eq", other);
        if (res == null) throw new RuntimeError("Unsupported operation '==' for object of type "+class_.name);
        return res;
    }
    public Value lt(Interpreter i, Value other) {
        Value res = tryOperator(i, "$lt", other);
        if (res == null) throw new RuntimeError("Unsupported operation '<' for object of type "+class_.name);
        return res;
    }
    public Value lte(Interpreter i, Value other) {
        Value res = tryOperator(i, "$lte", other);
        if (res == null) throw new RuntimeError("Unsupported operation '<=' for object of type "+class_.name);
        return res;
    }
    public Value gt(Interpreter i, Value other) {
        Value res = tryOperator(i, "$gt", other);
        if (res == null) throw new RuntimeError("Unsupported operation '>' for object of type "+class_.name);
        return res;
    }
    public Value gte(Interpreter i, Value other) {
        Value res = tryOperator(i, "$gte", other);
        if (res == null) throw new RuntimeError("Unsupported operation '>=' for object of type "+class_.name);
        return res;
    }
    public Value ne(Interpreter i, Value other) {
        Value res = tryOperator(i, "$ne", other);
        if (res == null) throw new RuntimeError("Unsupported operation '!=' for object of type "+class_.name);
        return res;
    }
    public Value neg(Interpreter i) {
        Value res = tryOperator(i, "$neg");
        if (res == null) throw new RuntimeError("Unsupported operation unary '-' for object of type "+class_.name);
        return res;
    }
    public Value uadd(Interpreter i) {
        Value res = tryOperator(i, "$uadd");
        if (res == null) throw new RuntimeError("Unsupported operation unary '+' for object of type "+class_.name);
        return res;
    }
    public Value getItem(Interpreter i, Value index) {
        Value res = tryOperator(i, "$getitem", index);
        if (res == null) throw new RuntimeError("Object of type "+class_.name+" is not subscripatble");
        return res;
    }
    public Value setItem(Interpreter i, Value index, Value v) {
        Value res = tryOperator(i, "$setitem", index, v);
        if (res == null) throw new RuntimeError("Item assignment not supported for object of type "+class_.name);
        return res;
    }
}