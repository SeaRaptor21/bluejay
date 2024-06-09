package com.esjr.bluejay;

class BluejayRegisteredMethod extends BluejayMethod implements BluejayCallable {
    final Value obj;
    
    BluejayRegisteredMethod(BluejayMethod method, Value obj) {
        super(method.declaration);
        this.obj = obj;
    }
    
    public Value call(Interpreter interpreter, java.util.List<Value> arguments) {
        return super.call(interpreter, arguments, obj);
    }
}