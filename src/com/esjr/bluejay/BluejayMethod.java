package com.esjr.bluejay;

import java.util.List;

class BluejayMethod extends Value {
    final Stmt.Method declaration;
    BluejayMethod(Stmt.Method declaration) {
        this.declaration = declaration;
    }

    public int arity() {
        return declaration.parameters.size();
    }

    public String toString(Interpreter i) {
        return "<method " + declaration.name.lexeme + ">";
    }
    public double toNumber(Interpreter i) {
        throw new RuntimeError.TypeError("Cannot convert method to number");
    }

    public Value call(Interpreter interpreter, List<Value> arguments, Value object) {
        Environment environment = new Environment(interpreter.environment);
        environment.define("this", object);
        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(((Token)(declaration.parameters.keySet().toArray()[i])).lexeme, arguments.get(i));
        }
        interpreter.executeBlock(((Stmt.Block)declaration.body).statements, environment);
        return new Value.Null();
    }

    public Value register(BluejayObj obj) {
        return new BluejayRegisteredMethod(this, obj);
    }
}