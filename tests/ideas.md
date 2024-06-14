# Custom operators
In Python, if you want to allow operators for a custom class,
you would use dunder methods, like `__add__` for the `+` operator.
This section details a few ideas for how to do this in Bluejay.
Personally, I find the double underscores a bit too clunky, and
my first proposal is that we prefix special methods with `$`, as in
`$add`. We also haven't used backticks yet, and I believe that in Mojo
you can use backticks to put illegal characters in an identifier.
We could possibly utalize this for operators, so maybe you can define
a method named `` `+` `` for the `+` operator, although this syntax
seems almost undiscoverable and quircky, and we would still need
a prefix for special methods like Python's `__str__`.

**Python code**
```py
class A:
    def __init__(self, v):
        self.value = v
    def __add__(self, other):
        return self.value+other.value

print(A(1)+A(1))
```

-------

**Equivalent Bluejay code** *(proposed)*
```js
class A {
    A(v) {
        this.value = v
    }
    
    $add(other) {
        return this.value+other.value
    }
}

print(A(1)+A(1))
```