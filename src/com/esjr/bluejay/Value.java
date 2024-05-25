package com.esjr.bluejay;

import java.util.*;

abstract class Value {
    public abstract java.lang.String toString();

    public Value add(Value other) {
        throw new RuntimeError("Unsupported operation '+' for object");
    }
    public Value sub(Value other) {
        throw new RuntimeError("Unsupported operation '-' for object");
    }
    public Value mul(Value other) {
        throw new RuntimeError("Unsupported operation '*' for object");
    }
    public Value div(Value other) {
        throw new RuntimeError("Unsupported operation '/' for object");
    }
    public Value mod(Value other) {
        throw new RuntimeError("Unsupported operation '%' for object");
    }
    public Value pow(Value other) {
        throw new RuntimeError("Unsupported operation '**' for object");
    }
    public Value eq(Value other) {
        throw new RuntimeError("Unsupported operation '==' for object");
    }
    public Value lt(Value other) {
        throw new RuntimeError("Unsupported operation '<' for object");
    }
    public Value lte(Value other) {
        throw new RuntimeError("Unsupported operation '<=' for object");
    }
    public Value gt(Value other) {
        throw new RuntimeError("Unsupported operation '>' for object");
    }
    public Value gte(Value other) {
        throw new RuntimeError("Unsupported operation '>=' for object");
    }
    public Value ne(Value other) {
        throw new RuntimeError("Unsupported operation '!=' for object");
    }
    public Value neg() {
        throw new RuntimeError("Unsupported operation unary '-' for object");
    }
    public Value uadd() {
        throw new RuntimeError("Unsupported operation unary '+' for object");
    }
    public Value getItem(Value index) {
        throw new RuntimeError("Cannot index on object");
    }
    public Value setItem(Value index, Value v) {
        throw new RuntimeError("Cannot index on object");
    }
    public Value getAttr(java.lang.String attr) {
        throw new RuntimeError("Unsupported operation '.' for object");
    }
    public Value setAttr(java.lang.String attr, Value v) {
        throw new RuntimeError("Unsupported operation '.' for object");
    }
    
    static class Number extends Value {
        public final double value;
        Number(double value) {
            this.value = value;
        }
        public java.lang.String toString() {
            java.lang.String str = java.lang.String.valueOf(value);
            return value%1 == 0 ? str.substring(0,str.length()-2) : str;
        }

        public Value add(Value other) {
            return new Number(value+((Number)other).value);
        }
        public Value sub(Value other) {
            return new Number(value-((Number)other).value);
        }
        public Value mul(Value other) {
            return new Number(value*((Number)other).value);
        }
        public Value div(Value other) {
            return new Number(value/((Number)other).value);
        }
        public Value mod(Value other) {
            return new Number(value%((Number)other).value);
        }
        public Value pow(Value other) {
            return new Number(Math.pow(value, ((Number)other).value));
        }
        public Value eq(Value other) {
            return new Boolean(value==((Number)other).value);
        }
        public Value lt(Value other) {
            return new Boolean(value<((Number)other).value);
        }
        public Value lte(Value other) {
            return new Boolean(value<=((Number)other).value);
        }
        public Value gt(Value other) {
            return new Boolean(value>((Number)other).value);
        }
        public Value gte(Value other) {
            return new Boolean(value<=((Number)other).value);
        }
        public Value ne(Value other) {
            return new Boolean(value!=((Number)other).value);
        }
        public Value neg() {
            return new Number(-value);
        }
        public Value uadd() {
            return new Number(value);
        }
        public Value getItem(Value index) {
            throw new RuntimeError("Cannot index on number");
        }
        public Value setItem(Value index, Value v) {
            throw new RuntimeError("Cannot index on number");
        }
        public Value getAttr(java.lang.String attr) {
            throw new RuntimeError("Unsupported operation '.' for number");
        }
        public Value setAttr(java.lang.String attr, Value v) {
            throw new RuntimeError("Unsupported operation '.' for number");
        }
    }

    static class String extends Value {
        public final java.lang.String value;
        String(java.lang.String value) {
            this.value = value;
        }
        public java.lang.String toString() {
            return value;
        }
    }

    static class Boolean extends Value {
        public final java.lang.Boolean value;
        Boolean(java.lang.Boolean value) {
            this.value = value;
        }
        public java.lang.String toString() {
            return value ? "true" : "false";
        }
    }

    static class List extends Value implements BluejayIterator {
        public final java.util.List<Value> elements;
        List(java.util.List<Value> elements) {
            this.elements = elements;
        }
        public java.lang.String toString() {
            java.util.List<java.lang.String> elems = new ArrayList<>();
            for (Value e : elements) {
                elems.add(e.toString());
            }
            return "[" + java.lang.String.join(", ", elems) + "]";
        }
        public java.util.List<Value> iter() {
            return elements;
        }
    }

    static class Dict extends Value implements BluejayIterator {
        public final Map<Value,Value> elements;
        Dict(Map<Value,Value> elements) {
            this.elements = elements;
        }
        public java.lang.String toString() {
            return "{" + java.lang.String.join(", ", (java.lang.String[])elements.keySet().stream().map(e -> e.toString()+": "+elements.get(e).toString()).toArray()) + "}";
        }
        public java.util.List<Value> iter() {
            java.util.List<Value> list = new ArrayList<Value>();
            list.addAll(elements.keySet());
            return list;
        }
    }

    static class Null extends Value {
        
        Null() {
            
        }
        public java.lang.String toString() {
            return "null";
        }
    }
}