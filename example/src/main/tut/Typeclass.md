Typeclass
=========

The challenge is to factor out the commonality here:

```tut
def sum(ns: List[Int]): Int = ns.foldRight(0)(_ + _)
def all(bs: List[Boolean]): Boolean = bs.foldRight(true)(_ && _)
def concat[A](ss: List[List[A]]): List[A] = ss.foldRight(List.empty[A])(_ ::: _)
```

Some examples

```tut
sum(List(1, 2, 3))
all(List(true, false, true))
concat(List(List('a', 'b'), List('c', 'd')))
```

In each example we have a call to `foldRight` (which works on any `List`), using a "zero" value and a combiner function that are specific to the list's element type. So let's factor out the type-specific part:

```tut
trait Combiner[A] {
  def combine(a: A, b: A): A
  def zero: A
}
```

With that, we can now factor out the common functionality:

```tut
def genericSum[A](as: List[A], c: Combiner[A]): A =
  as.foldRight(c.zero)(c.combine)
```

Let's define a combiner for Ints, using addition as our operator:

```tut
val intCombiner = new Combiner[Int] {
  def combine(a: Int, b: Int) = a + b
  def zero = 0
}

genericSum(List(1, 2, 3), intCombiner)
```

So `genericSum` works for _any type at all_, as long as you supply an appropriate `Combiner` for that type. This is the **typeclass pattern**: `Combiner` is the typeclass, and the `genericSum` method demands **evidence** that `A` has an associated **instance**.

Typeclass parameters are usually implicit, so let's rewrite a little:

```tut
def genericSum2[A](as: List[A])(implicit c: Combiner[A]): A =
  as.foldRight(c.zero)(c.combine)
```

Let's make our instance implicit, and declare another one:

```tut
implicit val IntCombiner = intCombiner // from above
implicit val BooleanCombiner = new Combiner[Boolean] {
  def combine(a: Boolean, b: Boolean): Boolean = a && b
  def zero = true
}

genericSum2(List(1, 2, 3))
genericSum2(List(true, false, true))
```

Ok this is pretty nice. We now have a generic function for summing stuff, we can only call it if there's an
associated `Combiner`, and it has the correct static type. Try it with a `List[String]` and it won't compile.


    genericSum2(List("foo", "bar", "baz"))) // won't compile

We can even use an implicit class to add this functionality as syntax. Because the `Combiner` instance in the constructor is implicit, it's also implicit in the body of the class.

```tut
implicit class CombinerSyntax[A](as: List[A])(implicit c: Combiner[A]) {
  def gsum: A = genericSum2(as) // c will be passed along because it's implicit here
}
List(1, 2, 3).gsum
List(true, false, true).gsum
```

But note that we never actually use `c` in the definition of `CombinerSyntax`; it's just there in order to be introduced to the implicit scope. For cases like this there is a shortcut syntax called a *context bound*.

```tut
implicit class CombinerSyntax2[A: Combiner](as: List[A]) {
  def gsum2: A = genericSum2(as) // unnamed Combiner[A] is implicit here
}
```

Let's create our `List` combiner. Note that this needs to be a `def` (not a `val`) because it has a type parameter. The compiler will call this method for us (!)

```tut
implicit def ListCombiner[A] = new Combiner[List[A]] {
  def combine(a: List[A], b: List[A]): List[A] = a ::: b
  def zero = List.empty[A]
}
```

And try it with the new syntax!

```tut
List(List('a', 'b'), List('c', 'd')).gsum2
```

While we're at it, let's add syntax for any combinable `A` as well!

```tut
implicit class ASyntax[A](a: A)(implicit c: Combiner[A]) {
  def |+|(b: A) = c.combine(a, b)
}

1 |+| 2
true |+| true |+| false
List(1, 2) |+| List(3, 4)
```

Ok this is where it gets crazy. If we have a `Combiner[A]` and a `Combiner[B]` can we make a `Combiner[(A,B)]`? I say we can, and the compiler will use this to construct `Combiner[(A, B)]` for _any_ `A` and `B` that can be combined.

```tut
implicit def PairCombiner[A, B](implicit ca: Combiner[A], cb: Combiner[B]): Combiner[(A, B)] =
  new Combiner[(A, B)] {
    def combine(a: (A, B), b: (A, B)): (A, B) = (a._1 |+| b._1, a._2 |+| b._2)
    def zero: (A, B) = (ca.zero, cb.zero)
  }

(1, true) |+| (2, true) |+| (3, true)
(List('a', 'b'), 5) |+| (List('d', 'e'), 10)
```

Note that summing now works for list of combinable pairs!

```tut
List((1, 2), (3, 4)).gsum
```

But because we can combine pairs, pairs are combinable. So we can combine nested pairs too!  WOW

```tut
val a = (1, ((true, 7), List('a', 'b')))
val b = (9, ((true, 8), List('c', 'd')))

a |+| b
```

Ok that's it for now. A few final notes:

* Congratulations, you have done some abstract algebra! The mathy name for `Combiner` is **Monoid**. In order to be correct we have to show that `zero |+| a == a` and `a |+| zero == a`. We have not done that here. With some luck we will do that in another example.

* We defined the additive monoid for integers and the conjunctive monoid for booleans, but both types have consistent monoids for other operations. What are they?

