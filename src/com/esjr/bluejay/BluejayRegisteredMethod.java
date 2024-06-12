package com.esjr.bluejay;

import java.util.*;

class BluejayRegisteredMethod extends BluejayMethod implements BluejayCallable {
    final BluejayMethod method;
    final Value obj;
    final int arity;
    
    BluejayRegisteredMethod(BluejayMethod method, Value obj) {
        super(method.declaration);
        this.method = method;
        this.obj = obj;
        arity = method.arity();
    }

    public int arity() {
        return arity;
    }
    
    public Value call(Interpreter interpreter, List<Value> arguments) {
        return super.call(interpreter, arguments, obj);
    }
}