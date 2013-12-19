# Functor

Executive summary:

 - A **Functor** is something you can map over. 
 - Mapping with the `identity` function has no effect.
 - Familiar examples include `List` and `Option`.
 - Less familiar examples include functions, where you can map over the result type. 

## Definition

More formally, a **Covariant Functor** is a defined by a type constructor `F[_]` together with a function 

    map : (A => B) => F[A] => F[B]

where the following laws hold:

1. `map identity` means the same thing as `identity`
2. `(map f) compose (map g)` means the same thing as `map (f compose g)`
  
Note that `map` is sometimes called `fmap`, and the argument order is sometimes reversed.

In common usage we say that type `A` *is* a functor when it is possible to define an instance of `Functor[A]`. Some people prefer to say that `A` *has* a functor.

## Standard Library

Functor-_like_ behavior is built into the Scala standard library, but Functor as an abstraction does
not exist in vanilla Scala. These invocations of `map` are consistent with the definition:

```scala
def add1(n:Int) = n + 1
Some(1).map(add1) // ok
List(1,2,3).map(add1) // ok
```

These examples of `map` are _not_ consistent with the defintion of Functor, so beware:

```scala
"abc".map(_.toUpper) // String has the wrong kind (i.e,. type-level arity)
Set(1,2,3,4,5,6).map(_ / 2) // Breaks parametricity; behavior depends on concrete element type
collection.mutable.ArrayBuffer(1,2,3).map(identity) // mutable; violates identity law
```

## Scalaz Representation

Scalaz provides the typeclass `Functor[F[_]]` which defines `map` as

    def map[A, B](fa: F[A])(f: A => B): F[B]
  
together with trait `FunctorLaw` which encodes the laws stated above (this can be used for testing that a given functor instance is lawful).


### Functor Instance

Let's define `Functor` instance for a simple container type, and then look at the other operations
that you get for free (all defined in terms of `map`). Here we will work directly with the Functor
instance; in a later section we will do the same things with library syntax.

```scala
import scalaz.Functor  
case class Box2[A](fst: A, snd: A)
implicit val boxFunctor = new Functor[Box2] { 
  def map[A, B](fa: Box2[A])(f: A => B): Box2[B] = Box2(f(fa.fst), f(fa.snd)) 
}
val F = Functor[Box2] 
```

#### Function Lifting

The fundamental `map` operation (which we defined) is also called `apply`.

```scala
F.map(Box2("123", "x"))(_.length)
F.apply(Box2("123", "x"))(_.length)
F(Box2("123", "x"))(_.length)
```

We can use `lift` to take a function `A => B` to `F[A] => F[B]`

```scala
F.lift((s:String) => s.length)(Box2("123", "x"))
```

`mapply` takes an `F[A => B]` and applies it to a value of type `A` to produce an `F[B]`.

```scala
def add1(n:Int) = n + 1
def times2(n:Int) = n * 2
F.mapply(10)(Box2(add1 _, times2 _))
```

#### Pairing

We can turn `A` into `(A,A)`.

```scala
F.fpair(Box2(true, false))
```

Or do this by injecting a constant of any type `B` on the right or left.

```scala
F.strengthL(1, Box2("abc", "x"))
F.strengthR(Box2("abc", "x"), 1)
```

Or pair each element with the result of applying a function.

```scala
F.fproduct(Box2("foo", "x"))(_.length)
```

#### Miscellaneous

We can empty our value of any information, retaining only structure:

```scala
F.void(Box2("foo", "x"))
```

We can turn a disjunction of `F`s into an `F` of disjunctions. This uses the disjunction type `\/`
from scalaz, which has the same meaning as `Either` but is a bit more convenient to use.

```scala
import scalaz.syntax.id._
F.counzip(Box2(1, 2).left[Box2[String]])
F.counzip(Box2(1, 2).right[Box2[String]])
```

#### Operations on Functors Themselves

So far we have seen operations that Functors provide for the types they describe. But Functors are
also values that can be composed in several ways.

We can `compose` functors, which lets us `map` over nested structures.

```scala
import scalaz.std.option._; import scalaz.std.list._ // functor instances
val f = Functor[List] compose Functor[Option] 
f.map(List(Some(1), None, Some(3)))(_ + 1)
```

We can also `bicompose` a functor with a **bifunctor** (tutorial forthcoming), yielding a new bifunctor.

```scala
// import scalaz.std.either._ // either bifunctor instance
// val f = Functor[List] bicompose Bifunctor[Either]
// f.bimap(List(Left(1), Right(2), Left(3)))(_ + 1, _ + 2)
"(Requires scalaz 7.1)"
```

The `product` of two functors is a functor over pairs.

```scala
val f = Functor[List] product Functor[Option]
f.map((List(1,2,3), Some(4)))(_ + 1)
```

**TODO**: `icompose`

### Functor Syntax

Scalaz provides syntax for types that have a `Functor` instance. Many of the operations we have
already seen are available this way:

```scala
import scalaz.syntax.functor._ // the syntax comes from here
val b2 = Box2("foo", "x")
b2.map(_.length)
b2.strengthL(true)
b2.strengthR(true)
b2.fpair
b2.fproduct(_.length)
b2.void
```

The  `as` operation (also called `>|`) replaces all elements with the given constant, preserving
structure. Note the similarity to the `void` operation.

```scala
b2 as 123
b2 >| false
```

The `fpoint` operation lifts the parameterized type into a given `Applicative`.

```scala
import scalaz.std.list._ 
b2.fpoint[List]
import scalaz.std.option._
b2.fpoint[Option]
```

The `distribute`, `cosequence`, and `cotraverse` operations require a `Distributive` instance for the
target type. **TODO**

### Provided Functor Instances

Scalaz provides functor (or better) instances for the following stdlib types. In many cases the functor instance is provided by a subtype of `Functor` such as `Monad`.

```scala
import scalaz._; import Scalaz._ // get all
Functor[java.util.concurrent.Callable]
Functor[List]
Functor[Option]
Functor[Stream]
Functor[Vector]
```

Either and its projections have functors when partially applied:

```scala
Functor[({type λ[α] = Either[String, α]})#λ] // Either, if left type param is fixed
Functor[({type λ[α] = Either.RightProjection[String, α]})#λ] // Right projection, if left type param is fixed
Functor[({type λ[α] = Either.LeftProjection[α, String]})#λ] // Left projection, if right type param is fixed
```

Function types are functors over their return type:

```scala
Functor[({type λ[α] = String => α})#λ] 
Functor[({type λ[α] = (String, Int) => α})#λ] 
Functor[({type λ[α] = (String, Int, Boolean) => α})#λ] // and so on, up to Function8
```

Tuple types are functors over their rightmost parameter:

```scala
Functor[({type λ[α] = (String, α)})#λ] 
Functor[({type λ[α] = (String, Int, α)})#λ] 
Functor[({type λ[α] = (String, Int, Boolean, α)})#λ] // and so on, up to Tuple8
```

**TODO** instances for scalaz types






