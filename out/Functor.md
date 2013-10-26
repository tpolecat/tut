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
```

## Scalaz

Scalaz provides the typeclass `Functor[F[_]]` which defines `map` as

    def map[A, B](fa: F[A])(f: A => B): F[B]
  
together with trait `FunctorLaw` which encodes the laws stated above.

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
boxFunctor: scalaz.Functor[Box2] = $anon$1@5b5db3e

scala> val F = Functor[Box2] 
F: scalaz.Functor[Box2] = $anon$1@5b5db3e
```

#### Function Lifting Operations

The fundamental `map` operation (which we defined) is also called `apply`.

```scala
scala> F.map(Box2("123", "x"))(_.length)
res4: Box2[Int] = Box2(3,1)

scala> F.apply(Box2("123", "x"))(_.length)
res5: Box2[Int] = Box2(3,1)

scala> F(Box2("123", "x"))(_.length)
res6: Box2[Int] = Box2(3,1)
```

We can use `lift` to take a function `A => B` to `F[A] => F[B]`

```scala
scala> F.lift((s:String) => s.length)(Box2("123", "x"))
res7: Box2[Int] = Box2(3,1)
```

`mapply` takes an `F[A => B]` and applies it to a value of type `A` to produce an `F[B]`.

```scala
scala> def add1(n:Int) = n + 1
add1: (n: Int)Int

scala> def times2(n:Int) = n * 2
times2: (n: Int)Int

scala> F.mapply(10)(Box2(add1 _, times2 _))
res8: Box2[Int] = Box2(11,20)
```

#### Pairing Operations

The operations `strengthL` and `strengthR` inject a constant paired element.

```scala
scala> F.strengthL(1, Box2("abc", "x"))
res9: Box2[(Int, String)] = Box2((1,abc),(1,x))

scala> F.strengthR(Box2("abc", "x"), 1)
res10: Box2[(String, Int)] = Box2((abc,1),(x,1))
```







