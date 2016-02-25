# changelog

This file summarizes **notable** changes for each release, but does not describe internal changes unless they are particularly exciting. For complete details please see the corresponding [milestones](https://github.com/tpolecat/tut/milestones?state=closed) and their associated issues.

### <a name="0.4.2"></a>New and Noteworthy for Version 0.4.2

- Fixed a bug that broke tab completion for multi-project builds.
- Initial support for Scala 2.12 and other build improvements courtesy of @guersam.

### <a name="0.4.1"></a>New and Noteworthy for Version 0.4.1

- **tut** is now linked to the sbt org, so an explicit resolver is no longer needed.
- New `:reset` and `:book` modifiers! See the README for details. Thanks @xuwei-k and @d6y!

### <a name="0.4.0"></a>New and Noteworthy for Version 0.4.0

- The `tutSourceDirectory` can now contain subdirectories, and can also contain non-text resources like images, which will be copied verbatim. The `tutNameFilter` setting specifies a regex for filenames to interpret (`.md` `.txt` `.htm` `.html` by default).
- The new `tutOnly` command allows you to run **tut** on a single file or subdirectory of `tutSourceDirectory`. Tab completion works.
- **tut** is now run in the `Test` scope, which means (a) you can include test examples, and (b) the **tut** runtime does not become a transitive dependency of your project.
- Newlines are preserved verbatim in `silent` blocks and in all definitions. In modes where REPL output is shown there is always exactly one blank line between statements, but otherwise newlines are neither removed nor introduced. This really improves formatting.
- The new `:fail` modifier asserts that the code in the shed *must* throw an exception or fail to compile, otherwise it fails the build.

