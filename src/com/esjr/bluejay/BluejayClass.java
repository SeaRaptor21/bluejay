package com.esjr.bluejay;

import java.util.*;

class BluejayClass extends Value implements BluejayCallable {
    public final Stmt.Class declaration;
    public String name;
    public final BluejayClass inherits;
    public final Map<String, Value> statics = new HashMap<>();
    BluejayClass(Stmt.Class declaration, BluejayClass inherits) {
        this.declaration = declaration;
        name = (declaration != null) ? declaration.name.lexeme : null;
        this.inherits = inherits;
        // Stmt.Method[] possibleInits = declaration.methods.toArray().filter(m -> m.name == declaration.name.lexeme);
        // if (possibleInits.count() == 1) {
        //     initializer = new BluejayFunction(possibleInits[0]);
        // } else {
        //     initializer = null;
        // }
        if (declaration != null) {
            for (Stmt mthd : declaration.methods) {
                statics.put(((Stmt.Method)mthd).name.lexeme, new BluejayMethod((Stmt.Method)mthd));
            }
        }
    }

    public Value getStatic(String name) {
        if (statics.containsKey(name)) return statics.get(name);
        if (inherits != null) {
            Value v = inherits.getStatic(name);
            if (v != null) return v;
        }
        return null;
    }

    public int arity() {
        if (!(statics.containsKey(name))) return 0;
        Value possibleInit = statics.get(name);
        if (!(possibleInit instanceof BluejayMethod)) throw new RuntimeError("The attribute name '"+name+"' is reserved for the initializer of "+name);
        return ((BluejayMethod)possibleInit).arity();
    }

    public String toString(Interpreter i) {
        return "<class "+name+">";
    }

    public Value call(Interpreter interpreter, List<Value> arguments) {
        BluejayObj obj = new BluejayObj(this);
        if (statics.containsKey(name)) {
            Value possibleInit = statics.get(name);
            if (!(possibleInit instanceof BluejayMethod)) throw new RuntimeError("The attribute name '"+name+"' is reserved for the initializer of "+name);
            ((BluejayMethod)possibleInit).call(interpreter, arguments, obj);
        }
        return obj;
    }
}