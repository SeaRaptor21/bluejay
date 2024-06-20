package com.esjr.bluejay;

import java.util.*;

abstract class NativeFunction extends Value implements BluejayCallable {
    int arity;
    String name;

    NativeFunction(int arity, String name) {
        this.arity = arity;
        this.name = name;
    }

    public int arity() {
        return arity;
    }

    public String toString(Interpreter i) {
        return "<built-in function "+name+">";
    }
    public double toNumber(Interpreter i) {
        throw new RuntimeError.TypeError("Cannot convert function to number");
    }
}