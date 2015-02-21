Foo

```scala
scala> 1 + 1
res0: Int = 2
```

Bar

```scala
2 + 2
```

Baz

```scala
scala> wut
<console>:9: error: not found: value wut
              wut
              ^
```

Qux

```scala
qut
```

Quux

```
scala> 42
res4: Int = 42
```

Should get a warning

```scala
scala> class Functor[F[_]]
warning: there was one feature warning; re-run with -feature for details
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
