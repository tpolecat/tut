# changelog

This file summarizes **notable** changes for each release, but does not describe internal changes unless they are particularly exciting. For complete details please see the corresponding [milestones](https://github.com/tpolecat/tut/milestones?state=closed) and their associated issues.

### <a name="0.4.0"></a>New and Noteworthy for Version 0.4.0

- The `tutSourceDirectory` can now contain subdirectories, and can also contain non-text resources like images, which will be copied verbatim. The `tutNameFilter` setting specifies a regex for filenames to interpret (`.md` `.txt` `.htm` `.html` by default).
- The new `tutOnly` command allows you to run **tut** on a single file or subdirectory of `tutSourceDirectory`. Tab completion works.
- **tut** is now run in the `Test` scope, which means (a) you can include test examples, and (b) the **tut** runtime does not become a transitive dependency of your project.
- Newlines are preserved verbatim in `silent` blocks and in all definitions. In modes where REPL output is shown there is always exactly one blank line between statements, but otherwise newlines are neither removed nor introduced. This really improves formatting.
- The new `:fail` modifier asserts that the code in the shed *must* throw an exception or fail to compile, otherwise it fails the build.

