package com.esjr.bluejay;

import java.util.*;

class NativeClass extends BluejayClass {
    NativeClass(String name, BluejayClass inherits) {
        super(null, inherits);
        this.name = name;
    }

    public void addStatic(String name, Value v) {
        statics.put(name, v);
    }

    public String toString(Interpreter i) {
        return "<built-in class " + name + ">";
    }

    public int arity() {
        if (!(statics.containsKey(name))) return 0;
        Value possibleInit = statics.get(name);
        if (!(possibleInit instanceof NativeMethod)) throw new RuntimeError("The attribute name '"+name+"' is reserved for the initializer of "+name);
        return ((NativeMethod)possibleInit).arity();
    }

    public Value call(Interpreter interpreter, List<Value> arguments) {
        BluejayObj obj = new BluejayObj(this);
        if (statics.containsKey(name)) {
            Value possibleInit = statics.get(name);
            if (!(possibleInit instanceof NativeMethod)) throw new RuntimeError("The attribute name '"+name+"' is reserved for the initializer of "+name);
            ((NativeMethod)possibleInit).call(interpreter, arguments, obj);
        }
        return obj;
    }
}