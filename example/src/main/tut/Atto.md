# atto

This is an intro tutorial for the [atto](https://github.com/tpolecat/atto) parser combinator library.

## Getting Started

Let's import some stuff.

```scala
import scalaz._, Scalaz._, atto._, Atto._, atto.parser.spire._
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

## A Larger Example

This is taken from a nice tutorial over at [FP Complete](https://www.fpcomplete.com/school/text-manipulation/attoparsec).

First let's define a data type for IP addresses.

```scala
import spire.math.UByte // we need this for unisigned bytes
case class IP(a: UByte, b: UByte, c: UByte, d: UByte) 
```

As a first pass we can parse an IP address in the form 128.42.30.1 by using the `ubyte` and 
`char` parsers directly, in a `for` comprehension.

```scala
val ip: Parser[IP] =
  for {
    a <- ubyte
    _ <- char('.')
    b <- ubyte
    _ <- char('.')
    c <- ubyte
    _ <- char('.')
    d <- ubyte
  } yield IP(a, b, c, d)

ip parseOnly "foo.bar"
ip parseOnly "128.42.42.1"
ip.parseOnly("128.42.42.1").option
```

Let's factor out the dot.

```scala
val dot: Parser[Char] =  char('.')
```

The `<~` and `~>` combinators combine two parsers sequentially, discarding the value produced by
the parser on the `~` side. We can use this to simplify our comprehension a bit.

```scala
val ip1: Parser[IP] =
  for { 
    a <- ubyte <~ dot
    b <- ubyte <~ dot
    c <- ubyte <~ dot
    d <- ubyte
  } yield IP(a, b, c, d)

ip1.parseOnly("128.42.42.1").option
```

We can name our parser, which provides slightly more enlightening failure messages

```scala
val ip2 = ip1 named "ip-address"
val ip3 = ip1 namedOpaque "ip-address" // difference is illustrated below

ip2 parseOnly "foo.bar"
ip3 parseOnly "foo.bar"
```

Since nothing that occurs on the right-hand side of our <- appears on the left-hand side, we
don't actually need a monad; we can use applicative syntax here.

```scala
val ubyteDot = ubyte <~ dot // why not?
val ip4 = (ubyteDot |@| ubyteDot |@| ubyteDot |@| ubyte)(IP.apply) as "ip-address"

ip4.parseOnly("128.42.42.1").option
```

We might prefer to get some information about failure, so `either` is an, um, option.

```scala
ip4.parseOnly("abc.42.42.1").either
ip4.parseOnly("128.42.42.1").either
```

Here's an example log. Let's write a parser for it.

```scala
val logData = 
  """|2013-06-29 11:16:23 124.67.34.60 keyboard
     |2013-06-29 11:32:12 212.141.23.67 mouse
     |2013-06-29 11:33:08 212.141.23.67 monitor
     |2013-06-29 12:12:34 125.80.32.31 speakers
     |2013-06-29 12:51:50 101.40.50.62 keyboard
     |2013-06-29 13:10:45 103.29.60.13 mouse
     |""".stripMargin
```

And some data types for the parsed data.

```scala
case class Date(year: Int, month: Int, day: Int)
case class Time(hour: Int, minutes: Int, seconds: Int)
case class DateTime(date: Date, time: Time)

sealed trait Product // Products are an enumerated type
case object Mouse extends Product
case object Keyboard extends Product
case object Monitor extends Product
case object Speakers extends Product

case class LogEntry(entryTime: DateTime, entryIP: IP, entryProduct: Product)
type Log = List[LogEntry]
```

There's no built-in parser for fixed-width ints, so we can just make one. Probably shouldn't
be doing this in a tutorial though. How should we handle this?

```scala
def fixed(n:Int): Parser[Int] =
  count(n, digit).map(_.mkString).flatMap { s => 
    try ok(s.toInt) catch { case e: NumberFormatException => err(e.toString) }
  }
```

Now we have what we need to put the log parser together.

```scala
val date: Parser[Date] =
  (fixed(4) <~ char('-') |@| fixed(2) <~ char('-') |@| fixed(2))(Date.apply)

val time: Parser[Time] =
  (fixed(2) <~ char(':') |@| fixed(2) <~ char(':') |@| fixed(2))(Time.apply)

val dateTime: Parser[DateTime] =
  (date <~ char(' ') |@| time)(DateTime.apply)

val product: Parser[Product] = {
  string("keyboard").map(_ => Keyboard) |
  string("mouse")   .map(_ => Mouse)    |
  string("monitor") .map(_ => Monitor)  |
  string("speakers").map(_ => Speakers)
}

val logEntry: Parser[LogEntry] =
  (dateTime <~ char(' ') |@| ip <~ char(' ') |@| product)(LogEntry.apply)

val log: Parser[Log] =
  sepBy(logEntry, char('\n'))

(log parseOnly logData).option.foldMap(_.mkString("\n"))
```



