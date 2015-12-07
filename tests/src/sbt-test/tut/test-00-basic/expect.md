Foo

```scala
scala> 1 + 1
res0: Int = 2
```

Bar

```scala
2 + 2
```

Baz

```scala
scala> wut
<console>:9: error: not found: value wut
              wut
              ^
```

Qux

```scala
qut
```

Quux

```
scala> 42
res4: Int = 42
```

Should get a warning

```scala
scala> class Functor[F[_]]
warning: there was one feature warning; re-run with -feature for details
defined class Functor
```

Should get an error

```scala
scala> new Functor[Either[String, ?]]
<console>:10: error: not found: type ?
              new Functor[Either[String, ?]]
                                         ^
<console>:10: error: Either[String,<error>] takes no type parameters, expected: one
              new Functor[Either[String, ?]]
                          ^
```

Should be hidden




Expr-interior newlines preserved in normal mode.

```scala
scala> val a = 1
a: Int = 1

scala> val b = 2
b: Int = 2

scala> val c = 3
c: Int = 3

scala> def foo(n: Int): String = {
     |   
     |   // interior space
     |   "bar"
     | 
     | }
foo: (n: Int)String
```

All newlines preserved in silent mode.

```scala
val a = 1
val b = 2
val c = 3

def foo(n: Int): String = {
  
  // interior space
  "bar"

}
```

This result is so long that the REPL truncates it to a bunch of `a`s with a `...` at the end.

```scala
scala> val thing = "a" * 1000
thing: String = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa...
```

```scala
scala> thing
<console>:9: error: not found: value thing
              thing
              ^
```

The end
