Should get a warning

```scala
scala> class Functor[F[_]]
warning: there was one feature warning; re-run with -feature for details
defined class Functor
```

Should NOT get an error

```scala
scala> object X extends Functor[Either[String, ?]]
defined object X
```
