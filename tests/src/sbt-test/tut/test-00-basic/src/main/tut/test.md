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

The end