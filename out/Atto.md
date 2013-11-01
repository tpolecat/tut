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

## A Larger Example

This is taken from a nice tutorial over at [FP Complete](https://www.fpcomplete.com/school/text-manipulation/attoparsec).

First let's define a data type for IP addresses.

```scala
scala> import spire.math.UByte // we need this for unisigned bytes
import spire.math.UByte

scala> case class IP(a: UByte, b: UByte, c: UByte, d: UByte) 
defined class IP
```

As a first pass we can parse an IP address in the form 128.42.30.1 by using the `ubyte` and 
`char` parsers directly, in a `for` comprehension.

```scala
scala> val ip: Parser[IP] =
     |   for {
     |     a <- ubyte
     |     _ <- char('.')
     |     b <- ubyte
     |     _ <- char('.')
     |     c <- ubyte
     |     _ <- char('.')
     |     d <- ubyte
     |   } yield IP(a, b, c, d)
ip: atto.Parser[IP] = (ubyte) flatMap ...

scala> ip parseOnly "foo.bar"
res20: atto.ParseResult[IP] = Fail(foo.bar,List(ubyte, int),Failure reading:bigInt)

scala> ip parseOnly "128.42.42.1"
res21: atto.ParseResult[IP] = Done(,IP(128,42,42,1))

scala> ip.parseOnly("128.42.42.1").option
res22: Option[IP] = Some(IP(128,42,42,1))
```

Let's factor out the dot.

```scala
scala> val dot: Parser[Char] =  char('.')
dot: atto.Parser[Char] = '.'
```

The `<~` and `~>` combinators combine two parsers sequentially, discarding the value produced by
the parser on the `~` side. We can use this to simplify our comprehension a bit.

```scala
scala> val ip1: Parser[IP] =
     |   for { 
     |     a <- ubyte <~ dot
     |     b <- ubyte <~ dot
     |     c <- ubyte <~ dot
     |     d <- ubyte
     |   } yield IP(a, b, c, d)
ip1: atto.Parser[IP] = ((ubyte) <~ '.') flatMap ...

scala> ip1.parseOnly("128.42.42.1").option
res23: Option[IP] = Some(IP(128,42,42,1))
```

We can name our parser, which provides slighExatly more enlightening failure messages

```scala
scala> val ip2 = ip1 as "ip-address"
ip2: atto.Parser[IP] = ip-address

scala> val ip3 = ip1 asOpaque "ip-address" // difference is illustrated below
ip3: atto.Parser[IP] = ip-address

scala> ip2 parseOnly "foo.bar"
res24: atto.ParseResult[IP] = Fail(foo.bar,List(ip-address, ubyte, int),Failure reading:bigInt)

scala> ip3 parseOnly "foo.bar"
res25: atto.ParseResult[IP] = Fail(foo.bar,List(),Failure reading:ip-address)
```

Since nothing that occurs on the right-hand side of our <- appears on the left-hand side, we
don't actually need a monad; we can use applicative syntax here.

```scala
scala> val ubyteDot = ubyte <~ dot // why not?
ubyteDot: atto.Parser[spire.math.UByte] = (ubyte) <~ '.'

scala> val ip4 = (ubyteDot |@| ubyteDot |@| ubyteDot |@| ubyte)(IP.apply) as "ip-address"
ip4: atto.Parser[IP] = ip-address

scala> ip4.parseOnly("128.42.42.1").option
res26: Option[IP] = Some(IP(128,42,42,1))
```

We might prefer to get some information about failure, so `either` is an, um, option.

```scala
scala> ip4.parseOnly("abc.42.42.1").either
res27: scalaz.\/[String,IP] = -\/(Failure reading:bigInt)

scala> ip4.parseOnly("128.42.42.1").either
res28: scalaz.\/[String,IP] = \/-(IP(128,42,42,1))
```

Here's an example log. Let's write a parser for it.

```scala
scala> val logData = 
     |   """|2013-06-29 11:16:23 124.67.34.60 keyboard
     |      |2013-06-29 11:32:12 212.141.23.67 mouse
     |      |2013-06-29 11:33:08 212.141.23.67 monitor
     |      |2013-06-29 12:12:34 125.80.32.31 speakers
     |      |2013-06-29 12:51:50 101.40.50.62 keyboard
     |      |2013-06-29 13:10:45 103.29.60.13 mouse
     |      |""".stripMargin
logData: String = 
"2013-06-29 11:16:23 124.67.34.60 keyboard
2013-06-29 11:32:12 212.141.23.67 mouse
2013-06-29 11:33:08 212.141.23.67 monitor
2013-06-29 12:12:34 125.80.32.31 speakers
2013-06-29 12:51:50 101.40.50.62 keyboard
2013-06-29 13:10:45 103.29.60.13 mouse
"
```

And some data types for the parsed data.

```scala
scala> case class Date(year: Int, month: Int, day: Int)
defined class Date

scala> case class Time(hour: Int, minutes: Int, seconds: Int)
defined class Time

scala> case class DateTime(date: Date, time: Time)
defined class DateTime

scala> sealed trait Product // Products are an enumerated type
defined trait Product

scala> case object Mouse extends Product
defined module Mouse

scala> case object Keyboard extends Product
defined module Keyboard

scala> case object Monitor extends Product
defined module Monitor

scala> case object Speakers extends Product
defined module Speakers

scala> case class LogEntry(entryTime: DateTime, entryIP: IP, entryProduct: Product)
defined class LogEntry

scala> type Log = List[LogEntry]
defined type alias Log
```

There's no built-in parser for fixed-width ints, so we can just make one. Probably shouldn't
be doing this in a tutorial though. How should we handle this?

```scala
scala> def fixed(n:Int): Parser[Int] =
     |   count(n, digit).map(_.mkString).flatMap { s => 
     |     try ok(s.toInt) catch { case e: NumberFormatException => err(e.toString) }
     |   }
fixed: (n: Int)atto.Parser[Int]
```

Now we have what we need to put the log parser together.

```scala
scala> val date: Parser[Date] =
     |   (fixed(4) <~ char('-') |@| fixed(2) <~ char('-') |@| fixed(2))(Date.apply)
date: atto.Parser[Date] = (((ok(<function2>)) flatMap ...) flatMap ...) flatMap ...

scala> val time: Parser[Time] =
     |   (fixed(2) <~ char(':') |@| fixed(2) <~ char(':') |@| fixed(2))(Time.apply)
time: atto.Parser[Time] = (((ok(<function2>)) flatMap ...) flatMap ...) flatMap ...

scala> val dateTime: Parser[DateTime] =
     |   (date <~ char(' ') |@| time)(DateTime.apply)
dateTime: atto.Parser[DateTime] = (((ok(<function2>)) flatMap ...) flatMap ...) flatMap ...

scala> val product: Parser[Product] = {
     |   string("keyboard").map(_ => Keyboard) |
     |   string("mouse")   .map(_ => Mouse)    |
     |   string("monitor") .map(_ => Monitor)  |
     |   string("speakers").map(_ => Speakers)
     | }
product: atto.Parser[Product] = (((("keyboard") map ...) | ...) | ...) | ...

scala> val logEntry: Parser[LogEntry] =
     |   (dateTime <~ char(' ') |@| ip <~ char(' ') |@| product)(LogEntry.apply)
logEntry: atto.Parser[LogEntry] = (((ok(<function2>)) flatMap ...) flatMap ...) flatMap ...

scala> val log: Parser[Log] =
     |   sepBy(logEntry, char('\n'))
log: atto.Parser[Log] = 
sepBy((((ok(<function2>)) flatMap ...) flatMap ...) flatMap ...,'
')

scala> (log parseOnly logData).option.foldMap(_.mkString("\n"))
res29: String = 
LogEntry(DateTime(Date(2013,6,29),Time(11,16,23)),IP(124,67,34,60),Keyboard)
LogEntry(DateTime(Date(2013,6,29),Time(11,32,12)),IP(212,141,23,67),Mouse)
LogEntry(DateTime(Date(2013,6,29),Time(11,33,8)),IP(212,141,23,67),Monitor)
LogEntry(DateTime(Date(2013,6,29),Time(12,12,34)),IP(125,80,32,31),Speakers)
LogEntry(DateTime(Date(2013,6,29),Time(12,51,50)),IP(101,40,50,62),Keyboard)
LogEntry(DateTime(Date(2013,6,29),Time(13,10,45)),IP(103,29,60,13),Mouse)
```



