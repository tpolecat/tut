# Functor

TL;DR a **Functor** is something you can map over. Familiar examples include `List` and `Option`; less familiar examples include functions, where you can map over the result type. 


## Definition

More formally, a **Covariant Functor** is a defined by a type constructor `F[_]` together with a function 

    map : (A => B) => F[A] => F[B]

where the following laws hold:

1. `map identity` means the same thing as `identity`
2. `(map f) compose (map g)` means the same thing as `map (f compose g)`
  
Note that `map` is sometimes called `fmap`, and the argument order is sometimes reversed.

## Standard Library

Functor-_like_ behavior is built into the Scala standard library, but Functor as an abstraction does
not exist in vanilla Scala. These invocations of `map` are consistent with the definition:

```scala
scala> def add1(n:Int) = n + 1
add1: (n: Int)Int

scala> Some(1).map(add1) // ok
res0: Option[Int] = Some(2)

scala> List(1,2,3).map(add1) // ok
res1: List[Int] = List(2, 3, 4)
```

These examples of `map` are _not_ consistent with the defintion of Functor, so beware:

```scala
scala> "abc".map(_.toUpper) // String has the wrong kind (i.e,. type-level arity)
res2: String = ABC

scala> Set(1,2,3,4,5,6).map(_ / 2) // Breaks parametricity; behavior depends on concrete element type
res3: scala.collection.immutable.Set[Int] = Set(2, 0, 3, 1)

scala> collection.mutable.ArrayBuffer(1,2,3).map(identity) // mutable; violates identity law
res4: scala.collection.mutable.ArrayBuffer[Int] = ArrayBuffer(1, 2, 3)
```

## Scalaz Representation

Scalaz provides the typeclass `Functor[F[_]]` which defines `map` as

    def map[A, B](fa: F[A])(f: A => B): F[B]
  
together with trait `FunctorLaw` which encodes the laws stated above.

Because Scala's `for` comprehensions desugar into calls to a set of methods that includes `map` and 
does not take implicit conversions into account, it is common practice to define `map` as an 
instance method on `A` and delegate to this method from the `Functor[A]` implementation.


### Functor Instance

Let's define `Functor` instance for a simple container type, and then look at the other operations
that you get for free (all defined in terms of `map`). Here we will work directly with the Functor
instance; in a later section we will do the same things with library syntax.

```scala
scala> import scalaz.Functor  
import scalaz.Functor

scala> case class Box2[A](fst: A, snd: A)
defined class Box2

scala> implicit val boxFunctor = new Functor[Box2] { 
     |   def map[A, B](fa: Box2[A])(f: A => B): Box2[B] = Box2(f(fa.fst), f(fa.snd)) 
     | }
boxFunctor: scalaz.Functor[Box2] = $anon$1@29472111

scala> val F = Functor[Box2] 
F: scalaz.Functor[Box2] = $anon$1@29472111
```

#### Function Lifting

The fundamental `map` operation (which we defined) is also called `apply`.

```scala
scala> F.map(Box2("123", "x"))(_.length)
res5: Box2[Int] = Box2(3,1)

scala> F.apply(Box2("123", "x"))(_.length)
res6: Box2[Int] = Box2(3,1)

scala> F(Box2("123", "x"))(_.length)
res7: Box2[Int] = Box2(3,1)
```

We can use `lift` to take a function `A => B` to `F[A] => F[B]`

```scala
scala> F.lift((s:String) => s.length)(Box2("123", "x"))
res8: Box2[Int] = Box2(3,1)
```

`mapply` takes an `F[A => B]` and applies it to a value of type `A` to produce an `F[B]`.

```scala
scala> def add1(n:Int) = n + 1
add1: (n: Int)Int

scala> def times2(n:Int) = n * 2
times2: (n: Int)Int

scala> F.mapply(10)(Box2(add1 _, times2 _))
res9: Box2[Int] = Box2(11,20)
```

#### Pairing

We can turn `A` into `(A,A)`.

```scala
scala> F.fpair(Box2(true, false))
res10: Box2[(Boolean, Boolean)] = Box2((true,true),(false,false))
```

Or do this by injecting a constant of any type `B` on the right or left.

```scala
scala> F.strengthL(1, Box2("abc", "x"))
res11: Box2[(Int, String)] = Box2((1,abc),(1,x))

scala> F.strengthR(Box2("abc", "x"), 1)
res12: Box2[(String, Int)] = Box2((abc,1),(x,1))
```

Or pair each element with the result of applying a function.

```scala
scala> F.fproduct(Box2("foo", "x"))(_.length)
res13: Box2[(String, Int)] = Box2((foo,3),(x,1))
```

#### Miscellaneous

We can empty our value of any information, retaining only structure:

```scala
scala> F.void(Box2("foo", "x"))
res14: Box2[Unit] = Box2((),())
```

We can turn a disjunction of `F`s into an `F` of disjunctions. This uses the disjunction type `\/`
from scalaz, which has the same meaning as `Either` but is a bit more convenient to use.

```scala
scala> import scalaz.syntax.id._
import scalaz.syntax.id._

scala> F.counzip(Box2(1, 2).left[Box2[String]])
res15: Box2[scalaz.\/[Int,String]] = Box2(-\/(1),-\/(2))

scala> F.counzip(Box2(1, 2).right[Box2[String]])
res16: Box2[scalaz.\/[String,Int]] = Box2(\/-(1),\/-(2))
```

#### Operations on Functors Themselves

So far we have seen operations that Functors provide for the types they describe. But Functors are
also values that can be composed in several ways.

We can `compose` functors, which lets us `map` over nested structures.

```scala
scala> import scalaz.std.option._; import scalaz.std.list._ // functor instances
import scalaz.std.option._
import scalaz.std.list._

scala> val f = Functor[List] compose Functor[Option] 
f: scalaz.Functor[[α]List[Option[α]]] = scalaz.Functor$$anon$1@224d12c8

scala> f.map(List(Some(1), None, Some(3)))(_ + 1)
res17: List[Option[Int]] = List(Some(2), None, Some(4))
```

We can also `bicompose` a functor with a **bifunctor** (tutorial forthcoming), yielding a new bifunctor.

```scala
scala> // import scalaz.std.either._ // either bifunctor instance
     | // val f = Functor[List] bicompose Bifunctor[Either]
     | // f.bimap(List(Left(1), Right(2), Left(3)))(_ + 1, _ + 2)
     | "(Requires scalaz 7.1)"
res21: String = (Requires scalaz 7.1)
```

The `product` of two functors is a functor over pairs.

```scala
scala> val f = Functor[List] product Functor[Option]
f: scalaz.Functor[[α](List[α], Option[α])] = scalaz.Functor$$anon$2@3466f0d2

scala> f.map((List(1,2,3), Some(4)))(_ + 1)
res22: (List[Int], Option[Int]) = (List(2, 3, 4),Some(5))
```

**TODO**: `icompose`

### Functor Syntax

Scalaz provides syntax for types that have a `Functor` instance. Many of the operations we have
already seen are available this way:

```scala
scala> import scalaz.syntax.functor._ // the syntax comes from here
import scalaz.syntax.functor._

scala> val b2 = Box2("foo", "x")
b2: Box2[String] = Box2(foo,x)

scala> b2.map(_.length)
res23: Box2[Int] = Box2(3,1)

scala> b2.strengthL(true)
res24: Box2[(Boolean, String)] = Box2((true,foo),(true,x))

scala> b2.strengthR(true)
res25: Box2[(String, Boolean)] = Box2((foo,true),(x,true))

scala> b2.fpair
res26: Box2[(String, String)] = Box2((foo,foo),(x,x))

scala> b2.fproduct(_.length)
res27: Box2[(String, Int)] = Box2((foo,3),(x,1))

scala> b2.void
res28: Box2[Unit] = Box2((),())
```

The  `as` operation (also called `>|`) replaces all elements with the given constant, preserving
structure. Note the similarity to the `void` operation.

```scala
scala> b2 as 123
res29: Box2[Int] = Box2(123,123)

scala> b2 >| false
res30: Box2[Boolean] = Box2(false,false)
```

The `fpoint` operation lifts the parameterized type into a given `Applicative`.

```scala
scala> import scalaz.std.list._ 
import scalaz.std.list._

scala> b2.fpoint[List]
res31: Box2[List[String]] = Box2(List(foo),List(x))

scala> import scalaz.std.option._
import scalaz.std.option._

scala> b2.fpoint[Option]
res32: Box2[Option[String]] = Box2(Some(foo),Some(x))
```

The `distribute`, `cosequence`, and `cotraverse` operations require a `Distributive` instance for the
target type. **TODO**

### Provided Functor Instances

**TODO**




