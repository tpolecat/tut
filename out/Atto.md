# atto

This is an intro tutorial for the [atto](https://github.com/tpolecat/atto) parser combinator library.

## Getting Started

Let's import some stuff.

```scala
scala> import scalaz._
import scalaz._

scala> import Scalaz._
import Scalaz._

scala> import atto._
import atto._

scala> import Atto._
import Atto._
```

Rock on, let's parse an integer!

```scala
scala> int parseOnly "123abc"
res0: atto.ParseResult[Int] = Done(abc,123)
```

## Very Simple Examples

A `Parser[A]` consumes characters and produces a value of type `A`. Let's look at a predefined parser that matches only
characters where `isLetter` is true.

```scala
scala> letter
res1: atto.Parser[Char] = letter
```

We can ask a parser to parse a string, and we get back a `ParseResult[A]`. The `Done` constructor shows the remaining input (if any) and the answer.

```scala
scala> letter.parse("x")
res2: atto.ParseResult[Char] = Done(,x)

scala> letter.parse("xyz")
res3: atto.ParseResult[Char] = Done(yz,x)
```

The `Failure` constructor shows us the remaining input, the parsing stack (ignore this for now), and a description
of the failiure.

```scala
scala> letter.parse("1")
res4: atto.ParseResult[Char] = Fail(1,List(),Failure reading:letter)
```

The `Partial` constructor indicates that the parser has neither succeeded nor failed; more input is required before we will know. We can `feed` more data to continue parsing. Our parsers thus support *incremental parsing*
which allows us to parse directly from a stream, for example.

```scala
scala> letter.parse("")
res5: atto.ParseResult[Char] = Partial(<function1>)

scala> letter.parse("").feed("abc")
res6: atto.ParseResult[Char] = Done(bc,a)
```

The `many` combinator turns a `Parser[A]` into a `Parser[List[A]]`.

```scala
scala> many(letter).parse("abc")
res7: atto.ParseResult[List[Char]] = Partial(<function1>)

scala> many(letter).parse("abc").feed("def")
res8: atto.ParseResult[List[Char]] = Partial(<function1>)
```

There may be more letters coming, so we can say we're `done` to indicate that there is no more input.

```scala
scala> many(letter).parse("abc").feed("def").done
res9: atto.ParseResult[List[Char]] = Done(,List(a, b, c, d, e, f))
```

`Parser` is a [functor](Functor.md).

```scala
scala> many(letter).map(_.mkString).parse("abc").feed("def").done
res10: atto.ParseResult[String] = Done(,abcdef)
```

The `~` combinator turns `Parser[A], Parser[B]` into `Parser[(A,B)]`

```scala
scala> letter ~ digit
res11: atto.Parser[(Char, Char)] = (letter) ~ digit

scala> (letter ~ digit).parse("a1")
res12: atto.ParseResult[(Char, Char)] = Done(,(a,1))

scala> (many(letter) ~ many(digit)).parse("aaa")
res13: atto.ParseResult[(List[Char], List[Char])] = Partial(<function1>)

scala> (many(letter) ~ many(digit)).parse("aaa").feed("bcd123").done
res14: atto.ParseResult[(List[Char], List[Char])] = Done(,(List(a, a, a, b, c, d),List(1, 2, 3)))

scala> (many(letter) ~ many(digit)).map(p => p._1 ++ p._2).parse("aaa").feed("bcd123").done
res15: atto.ParseResult[List[Char]] = Done(,List(a, a, a, b, c, d, 1, 2, 3))
```

Destructuring the pair in `map` is a pain, and it gets worse with nested pairs.

```scala
scala> (letter ~ int ~ digit ~ byte)
res16: atto.Parser[(((Char, Int), Char), Byte)] = (((letter) ~ int) ~ digit) ~ byte
```

But have no fear, `Parser` is an applicative functor.

```scala
scala> (many(letter) |@| many(digit))(_ ++ _).parse("aaa").feed("bcd123").done
res17: atto.ParseResult[List[Char]] = Done(,List(a, a, a, b, c, d, 1, 2, 3))
```

In fact, it's a monad. This allows the result of one parser to influence the behavior of subsequent parsers. Here we build a parser that parses an integer followed by an arbitrary string of that length.

```scala
scala> val p = for { n <- int; c <- take(n) } yield c
p: atto.Parser[String] = (int) flatMap ...

scala> p.parse("3abcdef")
res18: atto.ParseResult[String] = Done(def,abc)

scala> p.parse("4abcdef")
res19: atto.ParseResult[String] = Done(ef,abcd)
```




