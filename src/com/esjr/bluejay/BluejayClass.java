package com.esjr.bluejay;

import java.util.*;

class BluejayClass extends Value implements BluejayCallable {
    final Stmt.Class declaration;
    private final BluejayMethod initializer = null;
    private final Map<java.lang.String, BluejayMethod> methods = new HashMap<>();
    BluejayClass(Stmt.Class declaration) {
        this.declaration = declaration;
        // Stmt.Method[] possibleInits = declaration.methods.toArray().filter(m -> m.name == declaration.name.lexeme);
        // if (possibleInits.count() == 1) {
        //     initializer = new BluejayFunction(possibleInits[0]);
        // } else {
        //     initializer = null;
        // }
        for (Stmt mthd : declaration.methods) {
            methods.put(((Stmt.Method)mthd).name.lexeme, new BluejayMethod((Stmt.Method)mthd));
        }
    }

    public int arity() {
        return initializer == null ? 0 : initializer.arity();
    }

    public java.lang.String toString() {
        return "<class " + declaration.name.lexeme + ">";
    }

    public Value call(Interpreter interpreter, java.util.List<Value> arguments) {
        BluejayObj obj = new BluejayObj(this);
        if (initializer != null) initializer.call(interpreter, arguments, obj);
        return obj;
    }
}