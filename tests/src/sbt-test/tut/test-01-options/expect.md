Should NOT get a warning

```scala
scala> class Functor[F[_]]
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

Should get commented output, no prompt, no margin

```scala
val x = 1 + 2
// x: Int = 3
```

Should get commented error:

```scala
new Functor[Either[String, ?]]
// <console>:14: error: not found: type ?
//        new Functor[Either[String, ?]]
//                                   ^
// <console>:14: error: Either[String,<error>] takes no type parameters, expected: one
//        new Functor[Either[String, ?]]
//                    ^
```

Can comment multi-line input:

```scala
case class Fibble(
  factor:  Int,
  snazzle: Boolean
)
// defined class Fibble
```

Can comment multi-expression input:

```scala
import scala.collection.immutable.List
// import scala.collection.immutable.List

val y = 2
// y: Int = 2
```

The End
