# tut 

<img alt="confusion" align=right src="tut.jpg"/>

**tut** is a very simple documentation tool for Scala programs that reads Markdown files and interprets code in `scala` sheds. So you add tut as an [SBT](http://scala-sbt.org) plugin and then you can write tutorials that are typechecked and run as part of your build. The idea is to have tutorial code that is never out of sync with the code it's documenting.

There are some examples [here](out/), and their uninterpreted source is [here](example/src/main/tut).

### How-To

tut looks for code in `scala` sheds and replaces it with what you would see if you pasted the code into a REPL. As an example, the input file

    Here is how you add numbers:
    ```scala
    1 + 1
    ```

is rewritten as

    Here is how you add numbers:
    ```scala
    scala> 1 + 1
    res0: Int = 2    
    ```

The code runs from top to bottom (imports and definitions from earlier code blocks are available in subsequent blocks), with a new REPL session for each input file.

### Setting Up

Add the following to `project/plugins.sbt` in your project:

    resolvers += Resolver.url(
      "tpolecat-sbt-plugin-releases",
        url("http://dl.bintray.com/content/tpolecat/sbt-plugin-releases"))(
            Resolver.ivyStylePatterns)

    addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.2")

And add the following to `build.sbt`:

    tutSettings

This will add the following to your SBT world:

- `tutSourceDirectory` is where tut looks for input files. It is a file setting defaulting to `src/main/tut`.
- `tut` is a task that interprets **all files** in `tutSourceDirectory` and writes output to `target/<scala-version>/tut`. If the code fails to compile or otherwise barfs, you will get an error that directs you to the line where the failure happened.

### Particulars

- Code in `scala` sheds will be interpreted. Anything in your dependencies will be available. Interpreted code runs with the same classpath as `(Compile, doc)` with the addition of tut and its dependencies (scalaz 7.0 for now).
- Blank lines in the sheds are ignored. Multi-line definitions work, but `:paste` style definitions (for mutual recursion for example) don't work [yet].
- Each tutorial is an independent REPL session, and the code examples run from top to bottom.

### Complaints and other Feedback

Feedback of any kind is always appreciated. The intent for the next version is to provide some additional shed types with a prefix to allow uninterpreted `scala` sheds:

- `tut` replaces `scala` for default handling
- `tut:quiet` if output isn't desired (for class definitions, for example)
- `tut:paste` to mimic `:paste` in the REPL
- `tut:bogus` for code that is expected to not compile (this is a common thing to demonstrate).

Issues and PR's are welcome, or just find me on Twitter or `#scala` on FreeNode.

