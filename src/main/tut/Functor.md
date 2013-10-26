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

## Scalaz

Scalaz provides the typeclass `Functor[F[_]]` which defines `map` as

    def map[A, B](fa: F[A])(f: A => B): F[B]
  
together with trait `FunctorLaw` which encodes the laws stated above.

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

#### Function Lifting Operations

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

#### Pairing Operations

The operations `strengthL` and `strengthR` inject a constant paired element.

```scala
F.strengthL(1, Box2("abc", "x"))
F.strengthR(Box2("abc", "x"), 1)
```







