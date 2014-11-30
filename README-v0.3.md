# tut 

<img alt="How'd you get so funky?" align=right src="tut.jpg"/>

**tut** is a very simple documentation tool for Scala programs that reads Markdown files and interprets code in `tut` sheds. So you add tut as an [sbt](http://scala-sbt.org) plugin and then you can write tutorials that are typechecked and run as part of your build. The idea is to have tutorial code that is never out of sync with the code it's documenting.

The current version is **0.3.0**, which runs on **Scala 2.10** and **2.11** with **scalaz 7.1**.

There are some examples [here](out/), and their uninterpreted source is [here](example/src/main/tut).

### How-To

**tut** looks for code in `tut` sheds and (by default) replaces it with what you would see if you had pasted the code into a REPL. As an example, the input file

    Here is how you add numbers:
    ```tut
    1 + 1
    ```

is rewritten as

    Here is how you add numbers:
    ```scala
    scala> 1 + 1
    res0: Int = 2    
    ```

The code runs from top to bottom (imports and definitions from earlier code blocks are available in subsequent blocks), with a new REPL session for each input file.

You can follow `tut` with any number of colon-prefixed modifiers to alter the way a block is interpreted.

- Normally an error in interpretation causes the buid to fail, but if you want to include an example that fails to compile you can add the `nofail` modifier.
- If you don't want REPL prompts or responses you can use the `silent` modifier. Code is still interpreted and errors will cause the build to fail, but no REPL output will appear in the output.

For example

    This won't compile:
    ```tut:nofail
    blech?
    ```

Note that if you want a code block that is not interpreted at all, just use a normal `scala` shed; **tut** doesn't touch these.

### Setting Up

Add the following to `project/plugins.sbt` in your project to add SBT shell commands:

    resolvers += Resolver.url(
      "tpolecat-sbt-plugin-releases",
        url("http://dl.bintray.com/content/tpolecat/sbt-plugin-releases"))(
            Resolver.ivyStylePatterns)

    addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.3.0")

And add the following to `build.sbt` for the tut runtime, which must run alongside your code:

    tutSettings

This will add the following to your SBT world:

- `tutSourceDirectory` is where tut looks for input files. It is a file setting defaulting to `src/main/tut`.
- `tut` is a task that interprets **all files** in `tutSourceDirectory` and writes output to `target/<scala-version>/tut`. If the code fails to compile or otherwise barfs, you will get an error that directs you to the line where the failure happened. You can look at the partial output to see the full error message.

### Particulars

- Code in `scala` sheds will be interpreted. Anything in your dependencies will be available. Interpreted code runs with the same classpath as `(Compile, doc)` with the addition of tut and its dependencies (scalaz 7.0 for now).
- Blank lines in the sheds are ignored. Multi-line definitions work, but `:paste` style definitions (for mutual recursion for example) don't work [yet].
- Each tutorial is an independent REPL session, and the code examples run from top to bottom.

### Plans for 0.3

The intent for the next version is to provide some additional shed types with a prefix to allow uninterpreted `scala` sheds:

- `tut` replaces `scala` for default handling
- `tut:quiet` if output isn't desired (for class definitions, for example)
- `tut:paste` to mimic `:paste` in the REPL
- `tut:bogus` for code that is expected to not compile (this is a common thing to demonstrate).

Also hope to add cross-version support and improve error reporting.

### Complaints and other Feedback

Feedback of any kind is always appreciated. 

Issues and PR's are welcome, or just find me on Twitter or `#scala` on FreeNode.

