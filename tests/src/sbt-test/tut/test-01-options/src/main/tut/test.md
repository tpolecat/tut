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

The End
