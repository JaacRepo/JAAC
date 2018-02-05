package faulty

import abs.api.cwi.ABSFuture.done
import abs.api.cwi.ABSFutureSugar._
import abs.api.cwi.{ActorSystem, SugaredActor, TypedActor}

class WrongClosure extends SugaredActor with TypedActor[WrongClosure] {
  var state = 0

  class InternalActorsNotGoodIdea
      extends SugaredActor
      with TypedActor[InternalActorsNotGoodIdea] {
    def decrement = messageHandler {
      (1 to 10).foreach { _ => state -= 1 }
      done
    }
  }

  def increment = messageHandler {
    val badActor = new InternalActorsNotGoodIdea()
    badActor ! badActor.decrement
    (1 to 10).foreach { _ => state += 1 }
    println(state)
    done
  }
}

object RunWrongClosure extends SugaredActor {
  def main(args: Array[String]): Unit = {
    val wrongClosure = new WrongClosure()
    (wrongClosure ! wrongClosure.increment) onSuccess { _ =>
      ActorSystem.shutdown()
      done
    }
  }
}
