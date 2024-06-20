package com.esjr.bluejay;

import java.util.*;

abstract class Value {
    public abstract String toString(Interpreter i);
    public abstract double toNumber(Interpreter i);

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
    public Value getAttr(Interpreter i, Token attr) {
        throw new RuntimeError("Unsupported operation '.' for object");
    }
    public void setAttr(Interpreter i, String attr, Token operator, Value v) {
        throw new RuntimeError("Cannot assign attribute for object");
    }

    static class Number extends Value {
        public final double value;
        Number(double value) {
            this.value = value;
        }
        public String toString(Interpreter i) {
            String str = String.valueOf(value);
            return value%1 == 0 ? str.substring(0,str.length()-2) : str;
        }
        public double toNumber(Interpreter i) {
            return value;
        }

        public Value add(Interpreter i, Value other) {
            return new Number(value+((Number)other).value);
        }
        public Value sub(Interpreter i, Value other) {
            return new Number(value-((Number)other).value);
        }
        public Value mul(Interpreter i, Value other) {
            return new Number(value*((Number)other).value);
        }
        public Value div(Interpreter i, Value other) {
            return new Number(value/((Number)other).value);
        }
        public Value mod(Interpreter i, Value other) {
            return new Number(value%((Number)other).value);
        }
        public Value pow(Interpreter i, Value other) {
            return new Number(Math.pow(value, ((Number)other).value));
        }
        public Value eq(Interpreter i, Value other) {
            return new BluejayBoolean(value==((Number)other).value);
        }
        public Value lt(Interpreter i, Value other) {
            return new BluejayBoolean(value<((Number)other).value);
        }
        public Value lte(Interpreter i, Value other) {
            return new BluejayBoolean(value<=((Number)other).value);
        }
        public Value gt(Interpreter i, Value other) {
            return new BluejayBoolean(value>((Number)other).value);
        }
        public Value gte(Interpreter i, Value other) {
            return new BluejayBoolean(value<=((Number)other).value);
        }
        public Value ne(Interpreter i, Value other) {
            return new BluejayBoolean(value!=((Number)other).value);
        }
        public Value neg(Interpreter i) {
            return new Number(-value);
        }
        public Value uadd(Interpreter i) {
            return new Number(value);
        }
        public Value getItem(Interpreter i, Value index) {
            throw new RuntimeError("Cannot index on number");
        }
        public Value setItem(Interpreter i, Value index, Value v) {
            throw new RuntimeError("Cannot index on number");
        }
        public Value getAttr(Interpreter i, String attr) {
            throw new RuntimeError("Unsupported operation '.' for number");
        }
        public Value setAttr(Interpreter i, String attr, Value v) {
            throw new RuntimeError("Unsupported operation '.' for number");
        }
    }

    static class BluejayString extends Value {
        public final String value;
        BluejayString(String value) {
            this.value = value;
        }
        public String toString(Interpreter i) {
            return value;
        }
        public double toNumber(Interpreter i) {
            throw new RuntimeError.TypeError("Cannot convert string to number");
        }
    }

    static class BluejayBoolean extends Value {
        public final Boolean value;
        BluejayBoolean(Boolean value) {
            this.value = value;
        }
        public String toString(Interpreter i) {
            return value ? "true" : "false";
        }
        public double toNumber(Interpreter i) {
            return value ? 1 : 0;
        }
    }

    static class BluejayList extends Value implements BluejayIterator {
        public final List<Value> elements;
        BluejayList(List<Value> elements) {
            this.elements = elements;
        }
        public String toString(Interpreter i) {
            List<String> elems = new ArrayList<>();
            for (Value e : elements) {
                elems.add(e.toString(i));
            }
            return "[" + String.join(", ", elems) + "]";
        }
        public double toNumber(Interpreter i) {
            throw new RuntimeError.TypeError("Cannot convert list to number");
        }
        public List<Value> iter() {
            return elements;
        }
    }

    static class Dict extends Value implements BluejayIterator {
        public final Map<Value,Value> elements;
        Dict(Map<Value,Value> elements) {
            this.elements = elements;
        }
        public String toString(Interpreter i) {
            return "{" + String.join(", ", (String[])elements.keySet().stream().map(e -> e.toString()+": "+elements.get(e).toString()).toArray()) + "}";
        }
        public double toNumber(Interpreter i) {
            throw new RuntimeError.TypeError("Cannot convert dict to number");
        }
        public List<Value> iter() {
            List<Value> list = new ArrayList<Value>();
            list.addAll(elements.keySet());
            return list;
        }
    }

    static class Null extends Value {
        public String toString(Interpreter i) {
            return "null";
        }
        public double toNumber(Interpreter i) {
            return 0;
        }
    }
}