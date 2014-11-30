

before 1 + 1

```tut
1 + 1
```

after 1 + 1, before println

```tut
println("hello")
```

following is a tut shed

```tut
println("in a tut shed")
```

an error, which will be ignored

```tut:nofail
yo mama
```

an error with no prompt at all

```tut:nofail:silent
blech
```

this should have no prompt

```tut:silent
val a = 1 + 2
val b = "x" * 10
```

but the bindings should exist now.

```tut
(a, b)
```

this shouldn't be interpreted at all

```scala
foo
bar
```

the very end

