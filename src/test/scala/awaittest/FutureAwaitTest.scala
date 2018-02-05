package awaittest

import abs.api.cwi.{ActorSystem, SugaredActor, TypedActor}
import abs.api.cwi.ABSFuture.done

class FutureAwaitTest extends SugaredActor with TypedActor[FutureAwaitTest] {
  def m1 = messageHandler {
    this ! m2
  }

  def m2 = messageHandler {
    Thread.sleep(100)
    println("sleep finished")
    done
  }
}

object FutureAwaitTest extends SugaredActor {
  import abs.api.cwi.ABSFutureSugar._

  def main(args: Array[String]): Unit = {
    val actor = new FutureAwaitTest
    actor ! actor.m1 onSuccess ( _ => absMethod {
      println("done")
      ActorSystem.shutdown()
    })
  }
}
