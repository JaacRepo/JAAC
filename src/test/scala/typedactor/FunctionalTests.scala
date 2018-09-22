package typedactor

import com.ascoop.Future.done
import com.ascoop.{Future, TypedActor}
import com.ascoop.FutureFunctions._

object StartPoint {
  def main(args: Array[String]): Unit = {
    new FunctionalTests().start()
  }
}

class FunctionalTests extends TypedActor {
  def start() = messageHandler {
    val a2 = new FTest1
    val a3 = new FTest2

    val f1 = a2.doubler(2)
    val f2 = a3.tripler(2)

    f1 flatMap {
      x1 =>
        println(s"inside $x1")
        f2 map {
          x2 =>
            println(s"$x1 -> $x2")
            x1 + x2
        }
    } forEach println

    (for {
      x1 <- f1
      x2 <- f2
    } yield {
      println(s"Inside for: $x1, $x2")
      x1 + x2
    }) forEach println

    val nestedFut = f1 map {
      x1 =>
        println(s"inside2 $x1")
        f2 map {
          x2 =>
            println(s"$x1 -> $x2")
            x1 + x2
        }
    }
    nestedFut.flatten.forEach(println)
    nestedFut.flatten.forEach(println)

    println("this should end before other functions awaiting futures")

    done
  }
}


class FTest1 extends TypedActor {
  def doubler(a: Int): Future[Int] = messageHandler {
    done(a * 2)
  }
}

class FTest2 extends TypedActor {
  def tripler(a: Int): Future[Int] = messageHandler {
    done(a * 3)
  }
}
