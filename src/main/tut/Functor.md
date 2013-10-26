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
def add1(n:Int) = n + 1
Some(1).map(add1) // ok
List(1,2,3).map(add1) // ok
```

These examples of `map` are _not_ consistent with the defintion of Functor, so beware:

```scala
"abc".map(_.toUpper) // String has the wrong kind (i.e,. type-level arity)
Set(1,2,3,4,5,6).map(_ / 2) // Breaks parametricity; behavior depends on concrete element type
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

**TODO**

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

**TODO**




