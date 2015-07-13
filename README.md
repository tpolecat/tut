# tut 

<img src="https://api.travis-ci.org/tpolecat/tut.svg?branch=master"/><br>
[![Join the chat at https://gitter.im/tpolecat/tut](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/tpolecat/tut?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

<img alt="How'd you get so funky?" align=right src="tut.jpg"/>

**tut** is a very simple documentation tool for Scala that reads Markdown files and interprets Scala code in `tut` sheds, allowing you to write documentation that is typechecked and run as part of your build.

The current version is **0.4.0** (changelog [here](CHANGELOG.md)) which runs on **Scala 2.10** and **2.11**.

Projects using **tut** include [doobie](https://github.com/tpolecat/doobie) and [cats](https://github.com/non/cats). If you're using it and would like be added to the list, please submit a PR!

### Quick Start

**1**. Add the following to `project/plugins.sbt`:

```scala
resolvers += Resolver.url(
  "tpolecat-sbt-plugin-releases",
    url("http://dl.bintray.com/content/tpolecat/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.4.0")
```

**2**.  And add the following to `build.sbt`:

```scala
tutSettings
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

### Commands

**tut** adds the following commands:

| Command  | Explanation |
|----------|-------------|
| `tut`    | Moves the contents of `tutSourceDirectory` into `tutTargetDirectory`, interpreting code in `tut` sheds in any file whose name matches `tutNameFilter` (other files are copied but not interpreted). |
| `tutOnly` *<file>* | Does the same thing as `tut` but only for the specified path under `tutSourceDirectory`. Note that tab completion works for this command |

Interpretation obeys the following particulars:

- Each file is interpreted with an independent REPL session. Definitions earlier in the file are available later in the file.
- Each REPL has the same classpath as your build's `Test` configuration, and by default also has the same scalac options and compiler plugins.
- By default any error in interpretation (compilation failure or runtime exception) will cause the `tut` command to fail. If this command is part of your CI configuration then your build will fail. Yay!
- **tut** captures output from the REPL, as well as anything your code writes to standard output (`System.out`). ANSI escapes are removed from this output, so colorized console output will show up as plaintext.
- In modes that show REPL prompts (see below) blank lines *in between statements* are discarded and a single blank line is introduced between prompts, as in the normal REPL. Otherwise blank lines are neither introduced nor discarded. This is a change from prior versions.


### Modifiers

By default **tut** will interpret code in `tut` sheds as if it had been pasted into a Scala REPL. However sometimes you might want a definition without REPL noise, or might want to demonstrate non-compiling code (which would normally cause the build to fail). For these occasions **tut** provides a number of modifiers that you can add to the shed declaration. For instance,

    ```tut:silent
    import com.woozle.fnord._
    ```

will produce the following output, suppressing REPL noise:

    ```scala
    import com.woozle.fnord._
    ```

The following modifiers are supported. Note that you can use multiples if you like; for example you could use `tut:silent:fail` to show code that doesn't compile, without showing the compilation error.

| Modifier    | Explanation |
|-------------|-------------|
| `:fail`      | Code in the shed *must* throw an exception or fail to compile. Successful interpretation will cause a buid failure. |
| `:nofail`    | Code in the shed *might* throw an exception or fail to compile. Such failure will *not* cause a build failure. Note that this modifier is **deprecated** in favor of `:fail`. |
| `:silent`    | Suppresses REPL prompts and output; under this modifier the input and output text are identical. |
| `:plain`     | Output will not have `scala` syntax highlighting. |
| `:invisible` | Suppresses all output. This is not recommended since the point of **tut** is to provide code that the user can type in and expect to work. But in rare cases you might want one of these at the bottom of your file to clean up filesystem mess that your code left behind. |

### Settings

**tut** also adds the following sbt settings, all of which have reasonable defaults. It is unlikely that you will need to change any of them, but here you go.

| Setting | Explanation | Default Value |
|---------|-------------|---------------|
| `tutSourceDirectory` | Location of **tut** source files. | `sourceDirectory.value / "main" / "tut"` |
| `tutNameFilter`      | Regex specifying files that should be interpreted. | Names ending in `.md` `.txt` `.htm` `.html` |
| `tutTargetDirectory` | Destination for **tut** output. | `crossTarget.value / "tut"` |
| `tutScalacOptions`   | Compiler options that will be passed to the **tut** REPL. | Same as `Test` configuration. |
| `tutPluginJars`      | List of compiler plugin jarfiles to be passed to the **tut** REPL. | Same as `Test` configuration. |

### Integration with sbt-site

Tut is designed to work seamlessly with sbt-site so that your checked tutorials can be incorporated into your website.

Add the following to `project/plugins.sbt` in your project to add SBT shell commands:

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "0.8.1")
```

Then in your build sbt, link the files generated by tut to your site generation:

```scala
project("name").settings(site.addMappingsToSiteDir(tut, "tut"))
```

When the `makeSite` task is run in sbt, the typechecked tutorials from `src/main/tut` will be incorporated with the site generated by sbt-site in `target/site`.

### Complaints and other Feedback

Feedback of any kind is always appreciated. 

Issues and PR's are welcome, or just find me on Twitter or `#scala` on FreeNode or on [gitter](https://gitter.im/tpolecat/tut).


