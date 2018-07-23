Images

```tut
println("\033]1338;url=https://media.giphy.com/media/10RhccNxPSaglW/giphy.gif;alt=keycat\u0007")
```

```tut
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point
import java.io.File

val data = Seq.tabulate(100) { i =>
  Point(i.toDouble, scala.util.Random.nextDouble())
}
val bufferedImage = ScatterPlot(data).render().asBufferedImage
```

```tut:invisible
val out = java.util.Base64.getEncoder().wrap(Console.out)
print("\033]1337;File=blah;foo=whatever:")
javax.imageio.ImageIO.write(bufferedImage, "png", out)
print("\u0007")
```

wahoo ![wahoo](tut:1)

plot goes here ![lol](tut:0), hopefully
