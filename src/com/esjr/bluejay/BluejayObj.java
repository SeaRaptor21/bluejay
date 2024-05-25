package com.esjr.bluejay;

import java.util.List;

class BluejayObj extends Value {
    private final BluejayClass klass;
    public final Environment attributes = new Environment();
    BluejayObj(BluejayClass klass) {
        this.klass = klass;
    }

    public java.lang.String toString() {
        return "<" + klass.declaration.name.lexeme + " object>";
    }
}