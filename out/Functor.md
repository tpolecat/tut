Functor
=======

TL;DR a **Functor** is something you can map over. Familiar examples include `List` and `Option`; less familiar 
examples include functions, where you can map over the result type. 

Definition
----------

More formally, a **Covariant Functor** is a defined by a type constructor `F[_]` together with a function 

    map : (A => B) => F[A] => F[B]

where the following laws hold:

1. `map identity = identity`
2. `(map f) compose (map g) = map (f compose g)`
  
In the above laws `=` means equivalence, not value equality; i.e., replacing one with the other will not change 
the meaning of your program. Note that `map` is sometimes called `fmap`, and the argument order is sometimes reversed.


Scalaz Representation
---------------------

Scalaz provides the typeclass `Functor[F[_]]` which defines `map` as

    def map[A, B](fa: F[A])(f: A => B): F[B]
  
together with trait `FunctorLaw` which encodes the laws stated above.

Examples
--------

Let's define `Functor` for a simple container type.

```scala
scala> import scalaz.Functor  
import scalaz.Functor

scala> case class Box[A](a:A)
defined class Box

scala> implicit val boxFunctor = new Functor[Box] { 
     |   def map[A, B](fa: Box[A])(f: A => B): Box[B] = Box(f(fa.a)) 
     | }
boxFunctor: scalaz.Functor[Box] = $anon$1@1700cdc9

scala> val F = Functor[Box] 
F: scalaz.Functor[Box] = $anon$1@1700cdc9
```

The fundamental `map` operation (which we defined) is also called `apply`.

```scala
scala> F.map(Box("123"))(_.length)
res0: Box[Int] = Box(3)

scala> F.apply(Box("123"))(_.length)
res1: Box[Int] = Box(3)

scala> F(Box("123"))(_.length)
res2: Box[Int] = Box(3)
```

We can use `lift` to take a function `A => B` to `F[A] => F[B]`

```scala
scala> F.lift((s:String) => s.length)(Box("123"))
res3: Box[Int] = Box(3)
```

The operations `strengthL` and `strengthR` inject a constant paired element.

```scala
scala> F.strengthL(1, Box("abc"))
res4: Box[(Int, String)] = Box((1,abc))

scala> F.strengthR(Box(1), "abc")
res5: Box[(Int, String)] = Box((1,abc))
```
  
