package com.esjr.bluejay;

import java.util.*;

class BluejayFunction extends Value implements BluejayCallable {
    private final Stmt.Function declaration;
    BluejayFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    public int arity() {
        return declaration.parameters.size();
    }

    public java.lang.String toString() {
        return "<function " + declaration.name.lexeme + ">";
    }

    public Value call(Interpreter interpreter, java.util.List<Value> arguments) {
        Environment environment = new Environment(interpreter.globals);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(((Token)(declaration.parameters.keySet().toArray()[i])).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(((Stmt.Block)declaration.body).statements, environment);
        } catch (Interpreter.Return r) {
            return r.value;
        }
        return new Value.Null();
    }
}