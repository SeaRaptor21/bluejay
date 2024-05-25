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
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Value value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
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

    void assignAt(int distance, String name, Value value) {
        ancestor(distance).values.put(name, value);
    }
}