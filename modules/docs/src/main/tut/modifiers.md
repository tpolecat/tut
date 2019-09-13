---
layout: page
title:  "Modifiers"
section: "modifiers"
position: 3
---

### Modifiers

By default **tut** will interpret code in `tut` sheds as if it had been pasted into a Scala REPL. However sometimes you might want a definition without REPL noise, or might want to demonstrate non-compiling code (which would normally cause the build to fail). For these occasions **tut** provides a number of modifiers that you can add to the shed declaration. For instance,

```tut:invisible
object com { object woozle { object fnord }}
```

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
| `:fail`      | Code in the shed *must* throw an exception or fail to compile. Successful interpretation will cause a build failure. |
| `:nofail`    | Code in the shed *might* throw an exception or fail to compile. Such failure will *not* cause a build failure. Note that this modifier is **deprecated** in favor of `:fail`. |
| `:silent`    | Suppresses REPL prompts and output; under this modifier the input and output text are identical. |
| `:plain`     | Output will not have `scala` syntax highlighting. |
| `:invisible` | Suppresses all output. This is not recommended since the point of **tut** is to provide code that the user can type in and expect to work. But in rare cases you might want one of these at the bottom of your file to clean up filesystem mess that your code left behind. |
| `:book`      | Output will be suitable for copy and paste into the REPL. That is, there are no REPL prompts or margins, and output from the REPL is commented. |
| `:evaluated` | Suppresses REPL prompts and input statement, output only will be the evaluated statement. |
| `:passthrough`| Same as `evaluated` but code fences are also removed. Useful for code generating Markdown. |
| `:decorate(param)` | Decorates the output scala code block with `param`, enclosed in this way: `{: param }`, for use with Kramdown. You can add several `decorate` modifiers if you wish. |
| `:reset`    | Resets the REPL state prior to evaluating the code block. Use this option with care, as it has no visible indication and can be confusing to readers who are following along in their own REPLs. |
| `:paste`    | Code in the shed is pasted to the REPL. |
