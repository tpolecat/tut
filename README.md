# tut 

<img alt="How'd you get so funky?" align=right src="tut.jpg"/>

**tut** is a very simple documentation tool for Scala programs that reads Markdown files and interprets code in `tut` sheds. So you add **tut** as an [sbt](http://scala-sbt.org) plugin and then you can write tutorials that are typechecked and run as part of your build. The idea is to have tutorial code that is never out of sync with the code it's documenting.

The current version is **0.3.0**, which runs on **Scala 2.10** and **2.11** with **scalaz 7.1**.

**NOTE** this version is a **breaking change** with previous versions! **tut** now looks for `tut` sheds rather than `scala` sheds, so existing documentation will need to be modified when you upgrade. So be warned!

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
- If you don't want REPL prompts or responses you can use the `silent` modifier. Code is still interpreted and errors will cause the build to fail, but no REPL output will appear.
- If you don't want Scala syntax highlighting, use the `plain` modifier.

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
- Failures that are not in a `tut:nofail` block will cause the build to fail.

### Particulars

- Each tutorial is an independent REPL session, and the code examples run from top to bottom.
- Code in `tut` sheds will be interpreted. Anything in your dependencies will be available. Interpreted code runs with the same classpath as `(Compile, doc)` with the addition of tut and its dependencies (scalaz 7.1 for now).
- Blank lines in sheds are ignored. Multi-line definitions work, but `:paste` style definitions (for mutual recursion for example) don't work [yet].
- ANSI escapes in REPL output are filtered out.

### Complaints and other Feedback

Feedback of any kind is always appreciated. 

Issues and PR's are welcome, or just find me on Twitter or `#scala` on FreeNode.


