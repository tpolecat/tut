Foo

```tut
1 + 1
```

Bar

```tut:silent:decorate(#div-id-1)
2 + 2
```

Baz

```tut:nofail:decorate(#div-id-2)
wut
```

Qux

```tut:silent:nofail:decorate(#div-id-3):decorate(.class1)
qut
```

Quux

```tut:decorate(#div-id-4):decorate(.class1):plain
42
```

Should get a warning and should decorate the output block

```tut:decorate(.class2)
class Functor[F[_]]
```

Should get an error and should decorate the output block

```tut:decorate(#div-id-5):decorate(.class3):nofail
new Functor[Either[String, ?]]
```

Should be hidden

```tut:decorate(#div-id-6):decorate(.class1):invisible
println("hi")
```

Should be evaluated and should decorate the output block

```tut:decorate(#div-id-3):evaluated
println("Hi, I'm an evaluated expression")
```

Should be evaluated and the result is shown and should decorate the output block

```tut:evaluated:decorate(#div-id-6)
val sum = 2 + 2
```

Expr-interior newlines preserved in normal mode. Also, it should decorate the output block

```tut:decorate(.exclude)
val a = 1
val b = 2
val c = 3

def foo(n: Int): String = {
  
  // interior space
  "bar"

}
```

Multiple expressions in an :evaluated block are interpreted according to the code block, where the new lines are preserved. Additionally, it should decorate the output block.

```tut:decorate(#div-id-1):evaluated
val a = 2 + 2
val b = 3 + 3

def bar(c: Int): Int = {

  // interior space
  a + b + c
}
val result = bar(4)
println(result)
result
```

All newlines preserved in silent mode and it should decorate the output block.

```tut:decorate(#div-id-6):silent
val a = 1
val b = 2
val c = 3

def foo(n: Int): String = {
  
  // interior space
  "bar"

}
```

This result is so long that the REPL truncates it to a bunch of `a`s with a `...` at the end, decorating the output block at the end.

```tut:decorate(.exclude)
val thing = "a" * 1000
```

```tut:decorate(#the-end):fail:reset
thing
```

The end
