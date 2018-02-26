---
layout: page
title:  "Commands"
section: "commands"
position: 2
---

### Commands

**tut** adds the following sbt commands:

| Command  | Explanation |
|----------|-------------|
| `tut`    | Moves the contents of `tutSourceDirectory` into `tutTargetDirectory`, interpreting code in `tut` sheds in any file whose name matches `tutNameFilter` (other files are copied but not interpreted). |
| `tutQuick` | Like `tut` but compiles only files that have changed since last compilation. Note that this does *not* detect changes in Scala sources; it only looks at tut sources. |
| <code>tutOnly&nbsp;&lt;path&gt;</code> | Does the same thing as `tut` but only for the specified path under `tutSourceDirectory`. Note that tab completion works for this command |

Interpretation obeys the following particulars:

- Each file is interpreted with an independent REPL session. Definitions earlier in the file are available later in the file.
- Each REPL has the same classpath as your build's `Test` configuration, and by default also has the same scalac options and compiler plugins.
- By default any error in interpretation (compilation failure or runtime exception) will cause the `tut` command to fail. If this command is part of your CI configuration then your build will fail. Yay!
- **tut** captures output from the REPL, as well as anything your code writes to standard output (`System.out`). ANSI escapes are removed from this output, so colorized console output will show up as plaintext.
- In modes that show REPL prompts (see below) blank lines *in between statements* are discarded and a single blank line is introduced between prompts, as in the normal REPL. Otherwise blank lines are neither introduced nor discarded. This is a change from prior versions.
