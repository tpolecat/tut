Should get a warning

```tut
class Functor[F[_]]
```

Should NOT get an error

```tut:nofail
object X extends Functor[Either[String, ?]]
```
