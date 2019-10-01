Foo

```tut
1 + 1
```

Bar

```tut:silent
2 + 2
```

Baz

```tut:nofail
wut
```

Qux

```tut:silent:nofail
qut
```

Quux

```tut:plain
42
```

Should get a warning

```tut
class Functor[F[_]]
```

Should get an error

```tut:nofail
new Functor[Either[String, ?]]
```

Should be hidden

```tut:invisible
println("hi")
```

Should be evaluated

```tut:evaluated
println("Hi, I'm an evaluated expression")
```

Should be evaluated and the result is shown

```tut:evaluated
val sum = 2 + 2
```

Expr-interior newlines preserved in normal mode.

```tut
val a = 1
val b = 2
val c = 3

def foo(n: Int): String = {
  
  // interior space
  "bar"

}
```

Multiple expressions in an :evaluated block are interpreted according to the code block, where the new lines are preserved

```tut:evaluated
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

All newlines preserved in silent mode.

```tut:silent
val a = 1
val b = 2
val c = 3

def foo(n: Int): String = {
  
  // interior space
  "bar"

}
```

This result is so long that the REPL truncates it to a bunch of `a`s with a `...` at the end.

```tut
val thing = "a" * 1000
```

```tut:fail:reset
thing
```

Pasting.

```tut:paste
trait Show[A] { def show(a: A): String }
object Show {
  implicit val intShow: Show[Int] = new Show[Int] { def show(a: Int): String = a.toString }
}
```

Pasting again.

```tut:paste
trait Show2[A] { def show(a: A): String }
object Show2 {
  implicit val intShow: Show2[Int] = new Show2[Int] { def show(a: Int): String = a.toString }
}
val str = implicitly[Show2[Int]].show(123)
```

The end
