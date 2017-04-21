Should NOT get a warning

```tut
class Functor[F[_]]
```

Should get an error

```tut:nofail
new Functor[Either[String, ?]]
```

Should get commented output, no prompt, no margin

```tut:book
val x = 1 + 2
```

Should get commented error:

```tut:book:nofail
new Functor[Either[String, ?]]
```

Evaluated result is hidden when combining :evaluated and :silent modifiers

```tut:evaluated:silent
val sum = 2 + 2
```

Code block is evaluated and commented when combining :evaluated and :book modifiers

```tut:evaluated:book
val initialValue = 2 + 2
val list = List(1, 2, 3)
val sum = list.fold(0)(_ + _)
println(sum)
sum
```

Can comment multi-line input:

```tut:book
case class Fibble(
  factor:  Int,
  snazzle: Boolean
)
```

Can comment multi-expression input:

```tut:book
import scala.collection.immutable.List

val y = 2
```

Should not fail the build:

```tut:book
case class A(c: Class[_])
```

The End
