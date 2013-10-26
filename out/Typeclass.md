Typeclass
=========

A little typeclass example, taken from original source [here](https://github.com/tpolecat/examples/blob/master/src/main/scala/eg/Typeclass.scala). The tut output is kind of 
noisy but I think overall it's probably a win.

The challenge is to factor out the commonality here:

```scala
scala> def sum(ns: List[Int]): Int = ns.foldRight(0)(_ + _)
sum: (ns: List[Int])Int

scala> def all(bs: List[Boolean]): Boolean = bs.foldRight(true)(_ && _)
all: (bs: List[Boolean])Boolean

scala> def concat[A](ss: List[List[A]]): List[A] = ss.foldRight(List.empty[A])(_ ::: _)
concat: [A](ss: List[List[A]])List[A]
```

Some examples

```scala
scala> sum(List(1, 2, 3))
res0: Int = 6

scala> all(List(true, false, true))
res1: Boolean = false

scala> concat(List(List('a', 'b'), List('c', 'd')))
res2: List[Char] = List(a, b, c, d)
```

In each example we have a call to `foldRight` (which works on any `List`), using a "zero" value and a combiner function that are specific to the list's element type. So let's factor out the type-specific part:

```scala
scala> trait Combiner[A] {
     |   def combine(a: A, b: A): A
     |   def zero: A
     | }
defined trait Combiner
```

With that, we can now factor out the common functionality:

```scala
scala> def genericSum[A](as: List[A], c: Combiner[A]): A =
     |   as.foldRight(c.zero)(c.combine)
genericSum: [A](as: List[A], c: Combiner[A])A
```

Let's define a combiner for Ints, using addition as our operator:

```scala
scala> val intCombiner = new Combiner[Int] {
     |   def combine(a: Int, b: Int) = a + b
     |   def zero = 0
     | }
intCombiner: Combiner[Int] = $anon$1@1538b034

scala> genericSum(List(1, 2, 3), intCombiner)
res3: Int = 6
```

So `genericSum` works for _any type at all_, as long as you supply an appropriate `Combiner` for that type. This is the *typeclass pattern*: `Combiner` is the typeclass, and the `genericSum` method demands *evidence* that `A` has an associated *instance*.

Typeclass parameters are usually implicit, so let's rewrite a little:

```scala
scala> def genericSum2[A](as: List[A])(implicit c: Combiner[A]): A =
     |   as.foldRight(c.zero)(c.combine)
genericSum2: [A](as: List[A])(implicit c: Combiner[A])A
```

Let's make our instance implicit, and declare another one:

```scala
scala> implicit val IntCombiner = intCombiner // from above
IntCombiner: Combiner[Int] = $anon$1@1538b034

scala> implicit val BooleanCombiner = new Combiner[Boolean] {
     |   def combine(a: Boolean, b: Boolean): Boolean = a && b
     |   def zero = true
     | }
BooleanCombiner: Combiner[Boolean] = $anon$1@427ce5fc

scala> genericSum2(List(1, 2, 3))
res4: Int = 6

scala> genericSum2(List(true, false, true))
res5: Boolean = false
```

Ok this is pretty nice. We now have a generic function for summing stuff, we can only call it if there's an
associated `Combiner`, and it has the correct static type. Try it with a `List[String]` and it won't compile.


    genericSum2(List("foo", "bar", "baz"))) // won't compile

We can even use an implicit class to add this functionality as syntax. Because the `Combiner` instance in the constructor is implicit, it's also implicit in the body of the class.

```scala
scala> implicit class CombinerSyntax[A](as: List[A])(implicit c: Combiner[A]) {
     |   def gsum: A = genericSum2(as) // c will be passed along because it's implicit here
     | }
defined class CombinerSyntax

scala> List(1, 2, 3).gsum
res6: Int = 6

scala> List(true, false, true).gsum
res7: Boolean = false
```

But note that we never actually use `c` in the definition of `CombinerSyntax`; it's just there in order to be introduced to the implicit scope. For cases like this there is a shortcut syntax called a *context bound*.

```scala
scala> implicit class CombinerSyntax2[A: Combiner](as: List[A]) {
     |   def gsum2: A = genericSum2(as) // unnamed Combiner[A] is implicit here
     | }
defined class CombinerSyntax2
```

Let's create our `List` combiner. Note that this needs to be a `def` (not a `val`) because it has a type parameter. The compiler will call this method for us (!)

```scala
scala> implicit def ListCombiner[A] = new Combiner[List[A]] {
     |   def combine(a: List[A], b: List[A]): List[A] = a ::: b
     |   def zero = List.empty[A]
     | }
ListCombiner: [A]=> Combiner[List[A]]
```

And try it with the new syntax!

```scala
scala> List(List('a', 'b'), List('c', 'd')).gsum2
res8: List[Char] = List(a, b, c, d)
```

While we're at it, let's add syntax for any combinable `A` as well!

```scala
scala> implicit class ASyntax[A](a: A)(implicit c: Combiner[A]) {
     |   def |+|(b: A) = c.combine(a, b)
     | }
defined class ASyntax

scala> 1 |+| 2
res9: Int = 3

scala> true |+| true |+| false
res10: Boolean = false

scala> List(1, 2) |+| List(3, 4)
res11: List[Int] = List(1, 2, 3, 4)
```

Ok this is where it gets crazy. If we have a `Combiner[A]` and a `Combiner[B]` can we make a `Combiner[(A,B)]`? I say we can, and the compiler will use this to construct `Combiner[(A, B)]` for _any_ `A` and `B` that can be combined.

```scala
scala> implicit def PairCombiner[A, B](implicit ca: Combiner[A], cb: Combiner[B]): Combiner[(A, B)] =
     |   new Combiner[(A, B)] {
     |     def combine(a: (A, B), b: (A, B)): (A, B) = (a._1 |+| b._1, a._2 |+| b._2)
     |     def zero: (A, B) = (ca.zero, cb.zero)
     |   }
PairCombiner: [A, B](implicit ca: Combiner[A], implicit cb: Combiner[B])Combiner[(A, B)]

scala> (1, true) |+| (2, true) |+| (3, true)
res12: (Int, Boolean) = (6,true)

scala> (List('a', 'b'), 5) |+| (List('d', 'e'), 10)
res13: (List[Char], Int) = (List(a, b, d, e),15)
```

Note that summing now works for list of combinable pairs!

```scala
scala> List((1, 2), (3, 4)).gsum
res14: (Int, Int) = (4,6)
```

But because we can combine pairs, pairs are combinable. So we can combine nested pairs too!  WOW

```scala
scala> val a = (1, ((true, 7), List('a', 'b')))
a: (Int, ((Boolean, Int), List[Char])) = (1,((true,7),List(a, b)))

scala> val b = (9, ((true, 8), List('c', 'd')))
b: (Int, ((Boolean, Int), List[Char])) = (9,((true,8),List(c, d)))

scala> a |+| b
res15: (Int, ((Boolean, Int), List[Char])) = (10,((true,15),List(a, b, c, d)))
```

Ok that's it for now. A few final notes:

* Congratulations, you have done some abstract algebra! The mathy name for `Combiner` is *Monoid*. In order to be correct we have to show that `zero |+| a == a` and `a |+| zero == a`. We have not done that here. With some luck we will do that in another example.

* We defined the additive monoid for integers and the conjunctive monoid for booleans, but both types have consistent monoids for other operations. What are they?

