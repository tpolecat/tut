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

Evaluated result is hidden when combining :evaluated and :silent modifiers

```
```

Code block is evaluated and commented when combining :evaluated and :book modifiers

```
// initialValue: Int = 4
// list: List[Int] = List(1, 2, 3)
// sum: Int = 6
// 6
// res3: Int = 6
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

Should not fail the build:

```scala
case class A(c: Class[_])
// defined class A
```

The End
