package exceptionhandling

import abs.api.cwi.{ABSFuture, ActorSystem, SugaredActor}
import abs.api.cwi.ABSFutureSugar._

class MyActor extends SugaredActor {
  var healthy = true

  def faulty: ABSFuture[Void] = absVoidMethod {
    if (this.healthy) {
      this.healthy = false
      println("Healthy")
    } else {
      println("Sick")
      this.healthy = true  // for next time
      throw new RuntimeException("I am not feeling well...")
    }
  }
}

object Main extends SugaredActor {

  def main(args: Array[String]): Unit = {
    val actor = new MyActor()
    sequence(List(
      actor.send(() => actor.faulty),
      actor.send(() => actor.faulty),
      actor.send(() => actor.faulty)
    )) onSuccess (_ => absVoidMethod {
      ActorSystem.shutdown()
    })
  }

}
