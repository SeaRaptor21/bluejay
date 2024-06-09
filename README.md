# Bluejay
A small toy interpreted language with a recursive descent parser
and a tree-walking interpreter.
A full BNF-style grammar can be found at `grammar.txt`.

## Quick Start
Following are examples all of the basic functionalities of Bluejay.
A more complete example with comments can be found in `tests/fulltest.blu`.
```
print(1+2*3/4%5**6)

var x = 10
print(x)

var a = 10
{
    var a = 5
    print(a)
}
print (a)

if (1==1 and 1+1==2) {
    print("Math works!")
} else {
    print("Math is broken...")
}

for (var i=0; i<10; i++) {
    print(i)
}

foreach (var num in [1,2,3]) {
    print(num)
}

repeat (10) {
    print("repeating")
}

for (var x=0; x<3; x++) {
    for (var y=0; y<3; y++) {
        for (var z=0; z<3; z++) {
            print(z)
            break 2
        }
        print(y)
    }
    print(x)
}

func factorial(x) {
    if (x == 1) {
        return 1
    } else {
        return x*factorial(x-1)
    }
}
print(factorial(10))

class Test {
    Test(v) {
        this.value = v
    }
    add(other) {
        return this.value+other.value
    }
}

print(Test(5).add(Test(7)))
```

## Contributing
Feel free to make a pull request with additions to the language!
Be sure to include a good description of your additions and test
extensively, but almost everything is welcome.