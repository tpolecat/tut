
Fails because of "-Xfatal-warnings"

```scala
scala> trait Functor[F[_]]
<console>:12: warning: higher-kinded type should be enabled
by making the implicit value scala.language.higherKinds visible.
This can be achieved by adding the import clause 'import scala.language.higherKinds'
or by setting the compiler option -language:higherKinds.
See the Scaladoc for value scala.language.higherKinds for a discussion
why the feature should be explicitly enabled.
       trait Functor[F[_]]
                     ^
error: No warnings can be incurred under -Xfatal-warnings.
```

Is ok because unused imports warings are disabled during tut

```scala
scala> import scala.collection.immutable.List
import scala.collection.immutable.List
```

The End
