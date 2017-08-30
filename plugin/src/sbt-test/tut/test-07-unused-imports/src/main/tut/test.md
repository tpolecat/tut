
Fails because of "-Xfatal-warnings"

```tut:fail
trait Functor[F[_]]
```

Is ok because unused imports warings are disabled during tut

```tut
import scala.collection.immutable.List
```

The End
