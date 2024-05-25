package com.esjr.bluejay;

import java.util.*;

interface BluejayCallable {
    int arity();
    Value call(Interpreter interpreter, List<Value> arguments);
}