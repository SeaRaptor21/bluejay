package com.esjr.bluejay;

import java.util.*;

class NativeRegisteredMethod extends Value implements BluejayCallable {
    final NativeMethod method;
    final BluejayObj obj;
    final int arity;

    NativeRegisteredMethod(NativeMethod method, BluejayObj obj) {
        this.method = method;
        this.obj = obj;
        arity = method.arity;
    }

    public int arity() {
        return arity;
    }

    public String toString(Interpreter i) {
        return method.toString();
    }
    public double toNumber(Interpreter i) {
        throw new RuntimeError.TypeError("Cannot convert method to number");
    }

    public Value call(Interpreter interpreter, List<Value> arguments) {
        return method.call(interpreter, arguments, obj);
    }
}