package com.esjr.bluejay;

import java.util.*;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class Builtins {
    public static final Environment globals = new Environment();
    public static NativeClass listClass;
    public static NativeClass stringClass;
    public static NativeClass numberClass;
    public static NativeClass booleanClass;
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

        // String class
        stringClass = new NativeClass("String", null);
        stringClass.addStatic("String", new NativeMethod(1, "String") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                object.specialAttrs.put("value", arguments.get(0).toString(interpreter));
                return new Value.Null();
            }
        });
        stringClass.addStatic("replace", new NativeMethod(2, "replace") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                String replace;
                if (!(arguments.get(0) instanceof BluejayObj) || ((BluejayObj)arguments.get(0)).class_ != stringClass) {
                    throw new RuntimeError.TypeError("Thing to replace must be a string.");
                } else {
                    replace = (String)((BluejayObj)arguments.get(0)).specialAttrs.get("value");
                }
                String replaceWith;
                if (!(arguments.get(1) instanceof BluejayObj) || ((BluejayObj)arguments.get(1)).class_ != stringClass) {
                    throw new RuntimeError.TypeError("Thing to replace with must be a string.");
                } else {
                    replaceWith = (String)((BluejayObj)arguments.get(1)).specialAttrs.get("value");
                }
                return new Value.BluejayString(((String)object.specialAttrs.get("value")).replace(replace, replaceWith));
            }
        });
        stringClass.addStatic("$str", new NativeMethod(0, "$str") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return new Value.BluejayString((String)object.specialAttrs.get("value"));
            }
        });
        stringClass.addStatic("$bool", new NativeMethod(0, "$bool") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return new Value.BluejayBoolean(((String)object.specialAttrs.get("value")).length() > 0);
            }
        });
        stringClass.addStatic("$num", new NativeMethod(0, "$num") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                try {
                    return new Value.Number(Double.parseDouble((String)object.specialAttrs.get("value")));
                } catch (NumberFormatException e) {
                    throw new RuntimeError.TypeError("Cannot convert string with literal '"+(String)object.specialAttrs.get("value")+"' to number");
                }
            }
        });
        globals.define("String", stringClass);

        // Number class
        numberClass = new NativeClass("Number", null);
        numberClass.addStatic("Number", new NativeMethod(1, "Number") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                object.specialAttrs.put("value", arguments.get(0).toNumber(interpreter));
                return new Value.Null();
            }
        });
        numberClass.addStatic("$add", new NativeMethod(1, "$add") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot add number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(value+other));
                return (BluejayObj)Builtins.numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$sub", new NativeMethod(1, "$sub") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot subtract number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(value-other));
                return (BluejayObj)Builtins.numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$mul", new NativeMethod(1, "$mul") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot multiply number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(value*other));
                return (BluejayObj)Builtins.numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$div", new NativeMethod(1, "$div") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot divide number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                if (other == 0) throw new RuntimeError.ValueError("Division by zero");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(value/other));
                return (BluejayObj)Builtins.numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$mod", new NativeMethod(1, "$mod") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot modulo number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                if (other == 0) throw new RuntimeError.ValueError("Modulo by zero");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(value%other));
                return (BluejayObj)Builtins.numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$pow", new NativeMethod(1, "$pow") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot raise number to the powe of "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(Math.pow(value, other)));
                return (BluejayObj)Builtins.numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$eq", new NativeMethod(1, "$eq") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot compare number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.BluejayBoolean(value==other));
                return (BluejayObj)booleanClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$lt", new NativeMethod(1, "$lt") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot compare number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.BluejayBoolean(value<other));
                return (BluejayObj)booleanClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$lte", new NativeMethod(1, "$lte") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot compare number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.BluejayBoolean(value<=other));
                return (BluejayObj)booleanClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$gt", new NativeMethod(1, "$gt") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot compare number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.BluejayBoolean(value>other));
                return (BluejayObj)booleanClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$gte", new NativeMethod(1, "$gte") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot compare number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.BluejayBoolean(value>=other));
                return (BluejayObj)booleanClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$ne", new NativeMethod(1, "$ne") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot compare number and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.BluejayBoolean(value!=other));
                return (BluejayObj)booleanClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$neg", new NativeMethod(0, "$neg") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                List<Value> args = new ArrayList<>();
                args.add(new Value.Number(-value));
                return (BluejayObj)numberClass.call(interpreter, args);
            }
        });
        numberClass.addStatic("$uadd", new NativeMethod(0, "$uadd") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return object;
            }
        });
        numberClass.addStatic("$str", new NativeMethod(0, "$str") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                double value = (double)object.specialAttrs.get("value");
                if (value % 1 == 0) return new Value.BluejayString(String.valueOf((int)value));
                return new Value.BluejayString(String.valueOf(value));
            }
        });
        numberClass.addStatic("$num", new NativeMethod(0, "$num") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return object;
            }
        });
        numberClass.addStatic("$bool", new NativeMethod(0, "$bool") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return new Value.BluejayBoolean((double)object.specialAttrs.get("value") != 0);
            }
        });
        globals.define("Number", numberClass);

        // Boolean class
        booleanClass = new NativeClass("Boolean", null);
        booleanClass.addStatic("Boolean", new NativeMethod(1, "Boolean") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                object.specialAttrs.put("value", interpreter.truthy(arguments.get(0)));
                return new Value.Null();
            }
        });
        booleanClass.addStatic("$str", new NativeMethod(0, "$str") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return new Value.BluejayString((boolean)object.specialAttrs.get("value") ? "true" : "false");
            }
        });
        booleanClass.addStatic("$num", new NativeMethod(0, "$num") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return new Value.Number((boolean)object.specialAttrs.get("value") ? 1 : 0);
            }
        });
        booleanClass.addStatic("$bool", new NativeMethod(0, "$bool") {
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                return object;
            }
        });
        globals.define("Boolean", booleanClass);

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
        listClass.addStatic("$mul", new NativeMethod(0, "$mul") {
            @SuppressWarnings("unchecked")
            public Value call(Interpreter interpreter, List<Value> arguments, BluejayObj object) {
                List<Value> value = (List<Value>)object.specialAttrs.get("elements");
                Value otherObj = arguments.get(0);
                if (!(otherObj instanceof BluejayObj) || ((BluejayObj)otherObj).class_ != numberClass) {
                    throw new RuntimeError.TypeError("Cannot multiply List and "+interpreter.typeOf(otherObj));
                }
                double other = (double)((BluejayObj)otherObj).specialAttrs.get("value");
                List<Value> res = new ArrayList<>();
                for (int i=0; i<other; i++) {
                    for (Value v : value) {
                        res.add(v);
                    }
                }
                BluejayObj obj = (BluejayObj)listClass.call(interpreter, new ArrayList<Value>());
                obj.specialAttrs.put("elements", res);
                return obj;
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