package fibbonachi

import abs.api.cwi.{Future, TypedActor}

class FibActorAwaiting extends TypedActor{
  import TypedActor._

  def request(n: Int): Future[Int] = messageHandler {
    if (n <= 2) {
      Future.done(1)
    }
    else {
      val ff1 = (new FibActorAwaiting).request(n - 1)
      val ff2 = (new FibActorAwaiting).request(n - 2)
      List(ff1, ff2) onSuccessAll {ns =>
        Future.done(ns.head + ns.tail.head)
      }
    }
  }
}
