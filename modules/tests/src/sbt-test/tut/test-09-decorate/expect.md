Foo

```scala
scala> 1 + 1
res0: Int = 2
```

Bar

```scala
2 + 2
```
{: #div-id-1 }

Baz

```scala
scala> wut
<console>:13: error: not found: value wut
       wut
       ^
```
{: #div-id-2 }

Qux

```scala
qut
```
{: #div-id-3 .class1 }

Quux

```
scala> 42
res4: Int = 42
```
{: #div-id-4 .class1 }

Should get a warning and should decorate the output block

```scala
scala> class Functor[F[_]]
warning: there was one feature warning; re-run with -feature for details
defined class Functor
```
{: .class2 }

Should get an error and should decorate the output block

```scala
scala> new Functor[Either[String, ?]]
<console>:14: error: not found: type ?
       new Functor[Either[String, ?]]
                                  ^
<console>:14: error: Either[String,<error>] takes no type parameters, expected: one
       new Functor[Either[String, ?]]
                   ^
```
{: #div-id-5 .class3 }

Should be hidden




Should be evaluated and should decorate the output block

```
Hi, I'm an evaluated expression
```
{: #div-id-3 }

Should be evaluated and the result is shown and should decorate the output block

```
sum: Int = 4
```
{: #div-id-6 }

Expr-interior newlines preserved in normal mode. Also, it should decorate the output block

```scala
scala> val a = 1
a: Int = 1

scala> val b = 2
b: Int = 2

scala> val c = 3
c: Int = 3

scala> def foo(n: Int): String = {
     |   
     |   // interior space
     |   "bar"
     | 
     | }
foo: (n: Int)String
```
{: .exclude }

Multiple expressions in an :evaluated block are interpreted according to the code block, where the new lines are preserved. Additionally, it should decorate the output block.

```
a: Int = 4
b: Int = 6
bar: (c: Int)Int
result: Int = 14
14
res9: Int = 14
```
{: #div-id-1 }

All newlines preserved in silent mode and it should decorate the output block.

```scala
val a = 1
val b = 2
val c = 3

def foo(n: Int): String = {
  
  // interior space
  "bar"

}
```
{: #div-id-6 }

This result is so long that the REPL truncates it to a bunch of `a`s with a `...` at the end, decorating the output block at the end.

```scala
scala> val thing = "a" * 1000
thing: String = aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa...
```
{: .exclude }

```scala
scala> thing
<console>:13: error: not found: value thing
       thing
       ^
```
{: #the-end }

The end
