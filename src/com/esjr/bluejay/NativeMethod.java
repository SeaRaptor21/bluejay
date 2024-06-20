package com.esjr.bluejay;

import java.util.List;

abstract class NativeMethod extends Value {
    final String name;
    final int arity;

    NativeMethod(int arity, String name) {
        this.name = name;
        this.arity = arity;
    }

    public int arity() {
        return arity;
    }

    public String toString(Interpreter i) {
        return "<built-in method " + name + ">";
    }
    public double toNumber(Interpreter i) {
        throw new RuntimeError.TypeError("Cannot convert method to number");
    }

    abstract public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object);

    public Value register(BluejayObj obj) {
        return new NativeRegisteredMethod(this, obj);
    }
}