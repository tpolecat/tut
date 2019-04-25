---
layout: home

---
<img style="float: right;" src="img/tut.jpg"/>

# tut

[![Travis CI](https://travis-ci.org/tpolecat/tut.svg?branch=series%2F0.6.x)](https://travis-ci.org/tpolecat/tut)
[![Join the chat at https://gitter.im/tpolecat/tut](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tpolecat/tut?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

**tut** is a very simple documentation tool for Scala that reads Markdown files and interprets Scala code in `tut` sheds, allowing you to write documentation that is typechecked and run as part of your build.

The current version is **{{site.tutVersion}}** for **sbt 1.1** and **Scala {{site.scalaVersions}}** ({{site.scala213}}).

**tut** is a [Typelevel](http://typelevel.org/) project. This means we embrace pure, typeful, functional programming, and provide a safe and friendly environment for teaching, learning, and contributing as described in the Scala [Code of Conduct](http://scala-lang.org/conduct.html).

### Quick Start

**1**. Add the following to `project/plugins.sbt`:

```scala
addSbtPlugin("org.tpolecat" % "tut-plugin" % "{{site.tutVersion}}")
```

**2**.  And add the following to `build.sbt`:

```scala
enablePlugins(TutPlugin)
```

**3**.  Write a tutorial in `src/main/tut/Foo.md`:

    Here is how you add numbers:
    ```tut
    1 + 1
    ```

**4**.  At the `sbt>` prompt type `tut`, then look at the output in `target/<scala-version>/tut/Foo.md`:

    Here is how you add numbers:
    ```scala
    scala> 1 + 1
    res0: Int = 2
    ```

