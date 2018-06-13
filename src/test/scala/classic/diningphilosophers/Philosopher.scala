package classic.diningphilosophers

import abs.api.cwi.Future.done
import abs.api.cwi.{Future, ActorFsm, TypedActor}

sealed trait PState
case object Thinking extends PState
case class Hungry(left: Boolean = false, right: Boolean = false) extends PState
case object Eating extends PState

class Philosopher(name: String, left: Fork, right: Fork) extends TypedActor with ActorFsm {
  import TypedActor._

  override type TState = PState

  override def initState = Thinking

  def go: Future[Void] = stateHandler {
    case Thinking =>
      println(s"$name is becoming hungry")
      this.go
      goto (Hungry()) andReturn  done
    case hungry@Hungry(false, _) =>
      println(s"$name asking for left fork")
      left.acquire onSuccess {_ =>
        this.receiveForkLeft
      }
      goto(hungry) andReturn  done
    case hungry@Hungry(_, false) =>
      println(s"$name asking for right fork")
      right.acquire onSuccess {_ =>
        this.receiveForkRight
      }
      goto(hungry) andReturn done
    case Hungry(true, true) =>
      println(s"$name got both forks.")
      this.go
      goto(Eating) andReturn done
    case Eating =>
      println(s"$name is done eating")
      goto(Thinking) andReturn done
  }

  def receiveForkLeft = stateHandler {
    case Hungry(false, r) =>
      this.go
      goto(Hungry(true, r)) andReturn done
    case _ =>
      throw new RuntimeException(s"Bad state when receiving left fork: $name")
  }

  def receiveForkRight = stateHandler {
    case Hungry(l, false) =>
      this.go
      goto(Hungry(l, true)) andReturn done
    case _ =>
      throw new RuntimeException(s"Bad state when receiving right fork: $name")
  }
}
