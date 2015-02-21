Should NOT get a warning

```tut
class Functor[F[_]]
```

Should get an error

```tut:nofail
new Functor[Either[String, ?]]
```
