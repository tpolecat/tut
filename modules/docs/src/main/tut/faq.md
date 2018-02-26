---
layout: page
title:  "FAQ"
section: "faq"
position: 6
---

### Frequently-Asked Questions

#### I'm using `-Ywarn-unused-imports` and The REPL is freakout out!

Tut does not filter out `-Ywarn-unused-imports` from its `scalacOptions` anymore. If you need to re-enable that behaviour, simply add:

```scala
scalacOptions in Tut -= "-Ywarn-unused-import"
```

#### I have missing dependencies!

Tut does not inherit its CLASSPATH from the `Test` configuration anymore. If this breaks your build, you can add missing dependencies manually by using the `% "tut"` modifier. For example:

```scala
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % "tut"
```

#### How do I run the tests?

There are a set of test markdown files and corresponding expected markdown output from **tut**. Run these tests from sbt with:

```
+publishLocal
tests/scripted
```