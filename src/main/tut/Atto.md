# atto

This is an intro tutorial for the [atto](https://github.com/tpolecat/atto) parser combinator library.

## Getting Started

Let's import some stuff.

```scala
import scalaz._
import Scalaz._
import atto._
import Atto._
```

Rock on, let's parse an integer!

```scala
int parseOnly "123abc"
```

## Very Simple Examples

A `Parser[A]` consumes characters and produces a value of type `A`. Let's look at a predefined parser that matches only
characters where `isLetter` is true.

```scala
letter
```

We can ask a parser to parse a string, and we get back a `ParseResult[A]`. The `Done` constructor shows the remaining input (if any) and the answer.

```scala
letter.parse("x")
letter.parse("xyz")
```

The `Failure` constructor shows us the remaining input, the parsing stack (ignore this for now), and a description
of the failiure.

```scala
letter.parse("1")
```

The `Partial` constructor indicates that the parser has neither succeeded nor failed; more input is required before we will know. We can `feed` more data to continue parsing. Our parsers thus support *incremental parsing*
which allows us to parse directly from a stream, for example.

```scala
letter.parse("")
letter.parse("").feed("abc")
```

The `many` combinator turns a `Parser[A]` into a `Parser[List[A]]`.

```scala
many(letter).parse("abc")
many(letter).parse("abc").feed("def")
```

There may be more letters coming, so we can say we're `done` to indicate that there is no more input.

```scala
many(letter).parse("abc").feed("def").done
```

`Parser` is a [functor](Functor.md).

```scala
many(letter).map(_.mkString).parse("abc").feed("def").done
```

The `~` combinator turns `Parser[A], Parser[B]` into `Parser[(A,B)]`

```scala
letter ~ digit
(letter ~ digit).parse("a1")
(many(letter) ~ many(digit)).parse("aaa")
(many(letter) ~ many(digit)).parse("aaa").feed("bcd123").done
(many(letter) ~ many(digit)).map(p => p._1 ++ p._2).parse("aaa").feed("bcd123").done
```

Destructuring the pair in `map` is a pain, and it gets worse with nested pairs.

```scala
(letter ~ int ~ digit ~ byte)
```

But have no fear, `Parser` is an applicative functor.

```scala
(many(letter) |@| many(digit))(_ ++ _).parse("aaa").feed("bcd123").done
```

In fact, it's a monad. This allows the result of one parser to influence the behavior of subsequent parsers. Here we build a parser that parses an integer followed by an arbitrary string of that length.

```scala
val p = for { n <- int; c <- take(n) } yield c
p.parse("3abcdef")
p.parse("4abcdef")
```




