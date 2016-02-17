Ok

```scala
scala> 1 + 2
res0: Int = 3
```

Ok

```scala
scala> object Foo extends scala.util.control.NoStackTrace
defined object Foo
scala> throw Foo // to prevent output from having line numbers
Foo$
```

Ok

```scala
scala> woozle
<console>:13: error: not found: value woozle
       woozle
       ^
```

No good .. doesn't fail.

```scala
scala> 1 + 1
res3: Int = 2
```
