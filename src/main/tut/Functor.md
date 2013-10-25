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
import scalaz.Functor  
case class Box[A](a:A)

implicit val boxFunctor = new Functor[Box] { def map[A, B](fa: Box[A])(f: A => B): Box[B] = Box(f(fa.a)) }
val F = Functor[Box] 
```

The fundamental `map` operation (which we defined) is also called `apply`.

```scala
F.map(Box("123"))(_.length)
F.apply(Box("123"))(_.length)
F(Box("123"))(_.length)
```

We can use `lift` to take a function `A => B` to `F[A] => F[B]`

```scala
F.lift((s:String) => s.length)(Box("123"))
```

The operations `strengthL` and `strengthR` inject a constant paired element.

```scala
F.strengthL(1, Box("abc"))
F.strengthR(Box(1), "abc")
```
  
