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
<console>:13: error: not found: value wut
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
<console>:14: error: not found: type ?
       new Functor[Either[String, ?]]
                                  ^
<console>:14: error: Either[String,<error>] takes no type parameters, expected: one
       new Functor[Either[String, ?]]
                   ^
```

Should be hidden




Should be evaluated

```
Hi, I'm an evaluated expression
```

Should be evaluated and the result is shown

```
sum: Int = 4
```

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

Multiple expressions in an :evaluated block are interpreted according to the code block, where the new lines are preserved

```
a: Int = 4
b: Int = 6
bar: (c: Int)Int
result: Int = 14
14
res9: Int = 14
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
<console>:13: error: not found: value thing
       thing
       ^
```

The end
