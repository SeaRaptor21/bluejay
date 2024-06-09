package com.esjr.bluejay;

import java.util.*;

class BluejayClass extends Value implements BluejayCallable {
    final Stmt.Class declaration;
    final java.util.List<BluejayClass> inherits;
    private final Map<java.lang.String, Value> statics = new HashMap<>();
    BluejayClass(Stmt.Class declaration, java.util.List<BluejayClass> inherits) {
        this.declaration = declaration;
        this.inherits = inherits;
        // Stmt.Method[] possibleInits = declaration.methods.toArray().filter(m -> m.name == declaration.name.lexeme);
        // if (possibleInits.count() == 1) {
        //     initializer = new BluejayFunction(possibleInits[0]);
        // } else {
        //     initializer = null;
        // }
        for (Stmt mthd : declaration.methods) {
            statics.put(((Stmt.Method)mthd).name.lexeme, new BluejayMethod((Stmt.Method)mthd));
        }
    }

    public Value getStatic(java.lang.String name) {
        if (statics.containsKey(name)) return statics.get(name);
        for (BluejayClass c : inherits) {
            Value v = c.getStatic(name);
            if (v != null) return v;
        }
        return null;
    }

    public int arity() {
        if (!(statics.containsKey(declaration.name.lexeme))) return 0;
        Value possibleInit = statics.get(declaration.name.lexeme);
        if (!(possibleInit instanceof BluejayMethod)) throw new RuntimeError("The attribute name '"+declaration.name.lexeme+"' is reserved for the initializer of "+declaration.name.lexeme);
        return ((BluejayMethod)possibleInit).arity();
    }

    public java.lang.String toString() {
        return "<class " + declaration.name.lexeme + ">";
    }

    public Value call(Interpreter interpreter, java.util.List<Value> arguments) {
        BluejayObj obj = new BluejayObj(this);
        if (statics.containsKey(declaration.name.lexeme)) {
            Value possibleInit = statics.get(declaration.name.lexeme);
            if (!(possibleInit instanceof BluejayMethod)) throw new RuntimeError("The attribute name '"+declaration.name.lexeme+"' is reserved for the initializer of "+declaration.name.lexeme);
            ((BluejayMethod)possibleInit).call(interpreter, arguments, obj);
        }
        return obj;
    }
}