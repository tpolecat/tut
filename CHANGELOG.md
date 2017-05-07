# changelog

This file summarizes **notable** changes for each release, but does not describe internal changes unless they are particularly exciting. For complete details please see the corresponding [milestones](https://github.com/tpolecat/tut/milestones?state=closed) and their associated issues.

### <a name="0.5.0"></a>New and Noteworthy for Version 0.5.0

Many thanks to :sparkles: [Nicolas Rinaudo](https://github.com/nrinaudo) :sparkles: for his work on this release.

- **tut** is now an autoplugin.
- Added a `Tut` SBT configuration.
- Removed `tutScalacOptions` (replaced by `scalacOptions in Tut`).
- No longer filters `-Ywarn-unused-imports` out, but inherits the default `scalacOptions` from the REPL ones.
- `tutNameFilter` is now honoured when monitoring modified files.

### <a name="0.4.8"></a>New and Noteworthy for Version 0.4.8

- Added `decorate` modifier for use with Kramdown, courtesy of Juan Pedro Moreno.

### <a name="0.4.7"></a>New and Noteworthy for Version 0.4.7

- Added long-awaited `tutQuick` command, courtesy of Dave Gurnell.

### <a name="0.4.6"></a>New and Noteworthy for Version 0.4.6

- Added support for 2.12.0 final (thanks Lars Hupel).
- Improved management of Scala versions with scripted tests (thanks again Lars Hupel).

### <a name="0.4.5"></a>New and Noteworthy for Version 0.4.5

- Added support for 2.12.0-RC2 (thanks Lars Hupel).

### <a name="0.4.4"></a>New and Noteworthy for Version 0.4.4

- Added support for 2.12.0-RC1 (thanks BenyHill).
- Added `:evaluated` modifier to show *only* output (thanks Juan Pedro Moreno).

### <a name="0.4.3"></a>New and Noteworthy for Version 0.4.3

- Updated for 2.12.0-M4 and M5, both of which are in use at the moment.
- Error are now reported using canonical file paths, making it consistent with scalac and sbt (thanks Olivier Blanvillain).
- Compiler option `-Ywarn-unused-import` is now removed from options passed to `IMain` by **tut** since it makes the REPL freak out (thanks Jentsch).
- The `.markdown` extension is now included in the default name filter (thanks Chris Coffey).
- The `tut` task now returns the complete list of examined files; subdirectories were being ignored (thanks Cody Allen).
- The `tutOnly` filename completion parser is now available as a public setting, for evil purposes (thanks Adelbert Chang).
- The `scala-xml` version has been updated to `1.0.5` for compatibility with Scala 2.12 (thanks Tsukasa Kitachi).

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
