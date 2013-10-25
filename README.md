tut 
===

If you are here to **read** tutorials, please go [here](out) and enjoy!

![King Tut](tut.jpg)

authors
-------

If you are here to **write** tutorials, here's what you do:

- Clone this project, and add your tutorial to `src/main/tut`. By convention these are `.md` files.
- Code in `scala` sheds will be interpreted. Anything in the sbt dependencies will be available. See the [`Functor.md`](src/main/tut/Functor.md) source for an example.
- `sbt run` will rebuild all of the tutorials. If the REPL barfs at some point you will get an error message.
- Please commit everything (including the output in `out` so visitors can just consume tutorials without having to build them. At some point maybe we'll publish them elsewhere.

Known limitations:

- You can't do `:paste`-style definitions that are compiled together.

