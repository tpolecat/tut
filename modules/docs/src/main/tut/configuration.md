---
layout: page
title:  "Configuration"
section: "configuration"
position: 4
---

### Configuration

**tut** also adds the following sbt settings, all of which have reasonable defaults. It is unlikely that you will need to change any of them, but here you go.

| Setting | Explanation | Default Value |
|---------|-------------|---------------|
| `tutSourceDirectory`  | Location of **tut** source files. | `(sourceDirectory.value in Compile) / "tut"` |
| `tutNameFilter`       | Regex specifying files that should be interpreted. | Names ending in `.md` `.txt` `.htm` `.html` |
| `tutTargetDirectory`  | Destination for **tut** output. | `crossTarget.value / "tut"` |
| `scalacOptions in Tut` | Compiler options that will be passed to the **tut** REPL. | Same as `Test` configuration. |
| `tutPluginJars`       | List of compiler plugin jarfiles to be passed to the **tut** REPL. | Same as `Test` configuration. |

It's possible to add tut-specific dependencies through the `% "tut"` modifier.
For example:

```scala
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % "tut"
```
