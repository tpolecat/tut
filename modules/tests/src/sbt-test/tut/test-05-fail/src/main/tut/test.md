Ok

```tut
1 + 2
```

Ok

```tut:fail
object Foo extends scala.util.control.NoStackTrace
throw Foo // to prevent output from having line numbers
```

Ok

```tut:fail
woozle
```

No good .. doesn't fail.

```tut:fail
1 + 1
```
