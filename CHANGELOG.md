# changelog

This file summarizes **notable** changes for each release, but does not describe internal changes unless they are particularly exciting. For complete details please see the corresponding [milestones](https://github.com/tpolecat/tut/milestones?state=closed) and their associated issues.

### <a name="0.6.6"></a>New and Noteworthy for Version 0.6.7

- Scala 2.12.6, to fix bincompat issue in scala-reflect (see https://github.com/outr/scribe/issues/80)

### <a name="0.6.6"></a>New and Noteworthy for Version 0.6.6

- Scala 2.13.0-M4, thanks Tim Steinbach.

### <a name="0.6.5"></a>New and Noteworthy for Version 0.6.5

- `tutOnly` autocompletion fix from Cody Allen.
- Doc update from Channing Walton.

### <a name="0.6.4"></a>New and Noteworthy for Version 0.6.4

- **tut** now fails when the final code shed contains an incomplete expression (thanks Felix Mulder).
- Doc updates from Ryan Williams and David Francoeur.

### <a name="0.6.3"></a>New and Noteworthy for Version 0.6.3

- `fork in (Tut, run) := true` now works for real, sorry (thanks again @metasim).
- Build is now modern and uses `sbt-release` (finally!) so publishing is no longer a nightmare.
- There is now a wee microsite.

### <a name="0.6.2"></a>New and Noteworthy for Version 0.6.2

This release forward-ports improvements from 0.5.5 and 0.5.6.

- New `passthrough` modifier for code that generates markdown (thanks @metasim).
- `fork in (Tut, run) := true` now works (thanks again @metasim).
- `scalacOptions in Tut` now defaults to `scalacOptions in Test` which allows Scalameta to work properly (it doesn't work in console for some reason ... thanks @kailuowang and @suhasgaddam).
- Fixes stack overflows with large input files and cleans up the FP micro-library.

### <a name="0.6.1"></a>New and Noteworthy for Version 0.6.1

This updates scala-xml to 1.0.6 as required by Scala 2.13.0-M1, and reverts removal of 2.10 support. Thanks :sparkles: Frank Thomas :sparkles: for this contribution.

### <a name="0.6.0"></a>New and Noteworthy for Version 0.6.0

First release for sbt 1.0, courtesy of sparkly :sparkles: Lars Hupel :sparkles:.

------

*See the `master` branch for further developments in the 0.5.x series.*

### <a name="0.5.2"></a>New and Noteworthy for Version 0.5.2

Somehow :confused: tpolecat :confused: botched the 0.5.1 release, which didn't actually include the only change it was intended to include. This uh, fixes that.

### <a name="0.5.1"></a>New and Noteworthy for Version 0.5.1

This is a bugfix release that fixes an issue which could cause the tut-core dependency to be included in the POM information for artifacts of projects that use tut. Many thanks to :sparkles: [Nicolas Rinaudo](https://github.com/nrinaudo) :sparkles: and :sparkles: [Dale Wijnand](https://github.com/dwijnand) :sparkles: for prompt and clever sleuthing.

### <a name="0.5.0"></a>New and Noteworthy for Version 0.5.0

Many thanks to :sparkles: [Nicolas Rinaudo](https://github.com/nrinaudo) :sparkles: and :sparkles: [Jisoo Park](https://github.com/guersam) :sparkles: for their work on this release.

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
