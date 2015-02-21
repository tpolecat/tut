Should NOT get a warning

```scala
scala> class Functor[F[_]]
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
