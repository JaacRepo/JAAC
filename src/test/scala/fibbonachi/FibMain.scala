package fibbonachi

import abs.api.cwi.{Future, ActorSystem, TypedActor}

object FibMain {
  def main(args: Array[String]): Unit = {
    val N : Int  = 31
    val fjRunner = new FibActor(null, System.currentTimeMillis);
    val f = fjRunner.request(N)
  }
}


object FibMain2 extends TypedActor {

  val N = 25
  var rep = 7

  def main(args: Array[String]): Unit = {
    doFib
  }

  private def doFib: Unit = {
    val t1 = System.currentTimeMillis
    val fjRunner = new FibActorAwaiting
    fjRunner.request(N) onSuccess { result =>
      val t2 = System.currentTimeMillis
      println(s"Result for $N is $result in ${t2 - t1}")
      if (rep > 0) {
        rep -= 1
        doFib
      } else {
        ActorSystem.shutdown()
      }
      Future.done
    }
  }
}