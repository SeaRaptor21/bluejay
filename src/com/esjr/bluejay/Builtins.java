package com.esjr.bluejay;

import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Builtins {
    public static final Environment globals = new Environment();
    static {
        globals.define("print", new NativeFunction(1, "print") {
            public Value call(Interpreter interpreter, java.util.List<Value> arguments) {
                System.out.println(arguments.stream().map(Value::toString).collect(Collectors.joining(" ")));
                return new Value.Null();
            }
        });

        globals.define("input", new NativeFunction(1, "input") {
            public Value call(Interpreter interpreter, java.util.List<Value> arguments) {
                try {
                    InputStreamReader input = new InputStreamReader(System.in);
                    BufferedReader reader = new BufferedReader(input);
                    System.out.print(((Value.String)arguments.get(0)).value);
                    java.lang.String line = reader.readLine();
                    return new Value.String(line);
                } catch (IOException e) {
                    return new Value.Null();
                }
            }
        });
    }
}