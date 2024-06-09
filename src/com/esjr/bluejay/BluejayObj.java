package com.esjr.bluejay;

import java.util.List;

class BluejayObj extends Value {
    private final BluejayClass class_;
    public final Environment attributes = new Environment();
    BluejayObj(BluejayClass class_) {
        this.class_ = class_;
    }

    public Value getAttr(java.lang.String attr) {
        Value res;
        if (attributes.values.containsKey(attr)) res = attributes.values.get(attr);
        else res = class_.getStatic(attr);
        if (res instanceof BluejayMethod) {
            return ((BluejayMethod)res).register(this);
        }
        return res;
    }

    public void setAttr(java.lang.String attr, Token operator, Value v) {
        if (attributes.values.containsKey(attr)) {
            attributes.assignStr(attr, operator, v);
        } else {
            if (operator.type != TokenType.EQUAL)
                throw new RuntimeError.AttributeError(operator, "Cannot assign to undefined attribute '"+attr+"' of "+toString());
            attributes.define(attr, v);
        }
    }

    public java.lang.String toString() {
        return "<" + class_.declaration.name.lexeme + " object>";
    }
}