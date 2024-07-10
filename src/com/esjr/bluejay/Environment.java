package com.esjr.bluejay;

import java.util.*;

class Environment {
    final Environment enclosing;
    final Map<String, Value> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }
    
    void define(String name, Value value) {
        values.put(name, value);
    }

    Value get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);

        if (enclosing != null) return enclosing.get(name);
        
        throw new RuntimeError.NameError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Interpreter i, Token name, Token operator, Value value) {
        if (values.containsKey(name.lexeme)) {
            switch (operator.type) {
                case EQUAL:
                    values.put(name.lexeme, value);
                    break;
                case PLUS_EQUAL:
                    values.put(name.lexeme, values.get(name.lexeme).add(i, value));
                    break;
                case MINUS_EQUAL:
                    values.put(name.lexeme, values.get(name.lexeme).sub(i, value));
                    break;
                case STAR_EQUAL:
                    values.put(name.lexeme, values.get(name.lexeme).mul(i, value));
                    break;
                case SLASH_EQUAL:
                    values.put(name.lexeme, values.get(name.lexeme).div(i, value));
                    break;
                case PERCENT_EQUAL:
                    values.put(name.lexeme, values.get(name.lexeme).mod(i, value));
                    break;
                case STAR_STAR_EQUAL:
                    values.put(name.lexeme, values.get(name.lexeme).pow(i, value));
                    break;
                case PLUS_PLUS:
                    List<Value> args = new ArrayList<>();
                    args.add(new Value.Number(1));
                    Value one = Builtins.numberClass.call(i, args);
                    values.put(name.lexeme, values.get(name.lexeme).add(i, one));
                    break;
                case MINUS_MINUS:
                    args = new ArrayList<>();
                    args.add(new Value.Number(1));
                    one = Builtins.numberClass.call(i, args);
                    values.put(name.lexeme, values.get(name.lexeme).sub(i, one));
                    break;
                default:
                    throw new UnsupportedOperationException("Token "+operator.lexeme+" is not a valid assignment operator.");
            }
            return;
        }

        if (enclosing != null) {
            enclosing.assign(i, name, operator, value);
            return;
        }
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assignStr(Interpreter i, String name, Token operator, Value value) {
        if (values.containsKey(name)) {
            switch (operator.type) {
                case EQUAL:
                    values.put(name, value);
                    break;
                case PLUS_EQUAL:
                    values.put(name, values.get(name).add(i, value));
                    break;
                case MINUS_EQUAL:
                    values.put(name, values.get(name).sub(i, value));
                    break;
                case STAR_EQUAL:
                    values.put(name, values.get(name).mul(i, value));
                    break;
                case SLASH_EQUAL:
                    values.put(name, values.get(name).div(i, value));
                    break;
                case PERCENT_EQUAL:
                    values.put(name, values.get(name).mod(i, value));
                    break;
                case STAR_STAR_EQUAL:
                    values.put(name, values.get(name).pow(i, value));
                    break;
                    case PLUS_PLUS:
                        List<Value> args = new ArrayList<>();
                        args.add(new Value.Number(1));
                        Value one = Builtins.numberClass.call(i, args);
                        values.put(name, values.get(name).add(i, one));
                        break;
                    case MINUS_MINUS:
                        args = new ArrayList<>();
                        args.add(new Value.Number(1));
                        one = Builtins.numberClass.call(i, args);
                        values.put(name, values.get(name).sub(i, one));
                        break;
                default:
                    throw new UnsupportedOperationException("Token "+operator.lexeme+" is not a valid assignment operator.");
            }
            return;
        }

        if (enclosing != null) {
            enclosing.assignStr(i, name, operator, value);
            return;
        }
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing; 
        }

        return environment;
    }

    Value getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    void assignAt(Interpreter i, int distance, String name, Token operator, Value value) {
        Environment e = ancestor(distance);
        switch (operator.type) {
            case EQUAL:
                e.values.put(name, value);
                break;
            case PLUS_EQUAL:
                e.values.put(name, e.values.get(name).add(i, value));
                break;
            case MINUS_EQUAL:
                e.values.put(name, e.values.get(name).sub(i, value));
                break;
            case STAR_EQUAL:
                e.values.put(name, e.values.get(name).mul(i, value));
                break;
            case SLASH_EQUAL:
                e.values.put(name, e.values.get(name).div(i, value));
                break;
            case PERCENT_EQUAL:
                e.values.put(name, e.values.get(name).mod(i, value));
                break;
            case STAR_STAR_EQUAL:
                e.values.put(name, e.values.get(name).pow(i, value));
                break;
                case PLUS_PLUS:
                    List<Value> args = new ArrayList<>();
                    args.add(new Value.Number(1));
                    Value one = Builtins.numberClass.call(i, args);
                    e.values.put(name, e.values.get(name).add(i, one));
                    break;
                case MINUS_MINUS:
                    args = new ArrayList<>();
                    args.add(new Value.Number(1));
                    one = Builtins.numberClass.call(i, args);
                    e.values.put(name, e.values.get(name).sub(i, one));
                    break;
            default:
                throw new UnsupportedOperationException("Token "+operator.lexeme+" is not a valid assignment operator.");
        }
    }
}