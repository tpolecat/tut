tut 
===

If you are here to **read** tutorials, please go [here](out) and enjoy!

![King Tut](tut.jpg)

authors
-------

The deal is, this lets you write Scala tutorials that you can be sure will actually compile and 
work in the REPL. This helps to prevent the problem where tutorials have missing imports, use out
of date APIs, or just have dumb mistakes.

So if you want to **write** tutorials, here's what you do:

- Fork this project, and add your tutorial to `src/main/tut`. By convention these are `.md` files.
- Code in `scala` sheds will be interpreted. Anything in the sbt dependencies will be available. See the [`Functor.md`](src/main/tut/Functor.md) source for an example.
- Blank lines in the sheds are ignored. Multi-line definitions work, but `:paste` style definitions (for mutual recursion for example) don't work.
- Each tutorial is an independent REPL session, and the code examples run from top to bottom, so you don't
have to worry about your tutorial messing up someone else's (modulo resident compiler bugs, but let's not
think about this).
- `sbt run` will rebuild all of the tutorials. If the REPL barfs at some point you will get an error message, and you can look at the output and see where it starts to go funny.
- Please commit everything (including the output in `out`) so visitors can just consume tutorials without having to build them. At some point maybe we'll publish them elsewhere.

