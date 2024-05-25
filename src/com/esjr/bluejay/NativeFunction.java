package com.esjr.bluejay;

import java.util.*;

abstract class NativeFunction extends Value implements BluejayCallable {
    int arity;
    java.lang.String name;

    NativeFunction(int arity, java.lang.String name) {
        this.arity = arity;
        this.name = name;
    }

    public int arity() {
        return arity;
    }

    public java.lang.String toString() {
        return "<built-in function "+name+">";
    }
}