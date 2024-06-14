package com.esjr.bluejay;

import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Builtins {
    public static final Environment globals = new Environment();
    public static NativeClass listClass;
    static {
        // print function
        globals.define("print", new NativeFunction(1, "print") {
            public Value call(Interpreter interpreter, List<Value> arguments) {
                List<String> out = new ArrayList<>();
                for (Value v : arguments) {
                    out.add(v.toString(interpreter));
                }
                System.out.println(String.join(" ", out));
                return new Value.Null();
            }
        });

        // input function
        globals.define("input", new NativeFunction(1, "input") {
            public Value call(Interpreter interpreter, List<Value> arguments) {
                try {
                    InputStreamReader input = new InputStreamReader(System.in);
                    BufferedReader reader = new BufferedReader(input);
                    System.out.print(((Value.BluejayString)arguments.get(0)).value);
                    String line = reader.readLine();
                    return new Value.BluejayString(line);
                } catch (IOException e) {
                    return new Value.Null();
                }
            }
        });

        // List class
        listClass = new NativeClass("List", null);
        listClass.addStatic("List", new NativeMethod(0, "List") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                object.specialAttrs.put("elements", new ArrayList<Value>());
                return new Value.Null();
            }
        });
        listClass.addStatic("length", new NativeMethod(0, "length") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return new Value.Number(((List)object.specialAttrs.get("elements")).size());
            }
        });
        listClass.addStatic("append", new NativeMethod(1, "append") {
            // We KNOW that the "elements" attribute is a List<Value>,
            // that's the only thing we ever set it to. I may remove
            // the SuppressWarnings in the future, but they're not
            // a problem at the moment.
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                for (Value a : arguments) {
                    ((List<Value>)object.specialAttrs.get("elements")).add(a);
                }
                return new Value.Null();
            }
        });
        listClass.addStatic("$getitem", new NativeMethod(1, "$getitem") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                Value index = arguments.get(0);
                if (!(index instanceof Value.Number) || ((Value.Number)index).value % 1 != 0) throw new RuntimeError.TypeError("List indecies must be integers");
                int i = (int)((Value.Number)index).value;
                List<Value> elems = (List<Value>)object.specialAttrs.get("elements");
                if (i<0 || i>=elems.size()) throw new RuntimeError("Invalid index for list of length "+elems.size());
                return elems.get(i);
            }
        });
        listClass.addStatic("$setitem", new NativeMethod(1, "$setitem") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                Value index = arguments.get(0);
                if (!(index instanceof Value.Number) || ((Value.Number)index).value % 1 != 0) throw new RuntimeError.TypeError("List indecies must be integers");
                int i = (int)((Value.Number)index).value;
                List<Value> elems = (List<Value>)object.specialAttrs.get("elements");
                if (i<0 || i>=elems.size()) throw new RuntimeError("Invalid index for list of length "+elems.size());
                elems.set(i, arguments.get(1));
                return new Value.Null();
            }
        });
        listClass.addStatic("$str", new NativeMethod(0, "$str") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                List<String> elems = new ArrayList<>();
                for (Value e : (List<Value>)object.specialAttrs.get("elements")) {
                    elems.add(e.toString(interpreter));
                }
                return new Value.BluejayString("[" + String.join(", ", elems) + "]");
            }
        });
        globals.define("List", listClass);
    }
}