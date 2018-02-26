---
layout: page
title:  "Standalone Usage"
section: "standalone"
position: 1
---

### Standalone Usage

In case you want to run **tut** without sbt, you can use **coursier** instead.

**1**. Install the **coursier** [command-line launcher](https://github.com/alexarchambault/coursier#command-line-1).

**2**. Run **tut**:

```
coursier launch -r "https://dl.bintray.com/tpolecat/maven/" org.tpolecat:tut-core_2.12:{{site.tutVersion}} -- \
  in out '.*\.md$' -classpath $(coursier fetch -p com.chuusai:shapeless_2.12:2.3.3)
```

This will process all `*.md` files in `in`, write them to `out`, while providing `com.chuusai:shapeless_2.11:2.3.1` in the classpath.
Note that the Scala library always needs to be in the classpath.