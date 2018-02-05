package classic.diningphilosophers

import abs.api.cwi.{ActorFsm, SugaredActor, TypedActor}
import abs.api.cwi.ABSFuture.done
import abs.api.cwi.ABSFutureSugar._

class Philosopher(name: String, left: Fork, right: Fork) extends SugaredActor with TypedActor[Philosopher] with ActorFsm[Philosopher] {
  sealed trait PState extends AbstractState
  case object Thinking extends PState
  case class Hungry(left: Boolean = false, right: Boolean = false) extends PState
  case object Eating extends PState

  override type TState = PState

  override def initState = Thinking

  def go: Message[Void] = stateHandler {
    case Thinking =>
      println(s"$name is becoming hungry")
      this ! go
      (Hungry(), done)
    case hungry@Hungry(false, _) =>
      println(s"$name asking for left fork")
      (left ! left.acquire) onSuccess {_ =>
        this ! receiveForkLeft
      }
      (hungry, done)
    case hungry@Hungry(_, false) =>
      println(s"$name asking for right fork")
      (right ! right.acquire) onSuccess {_ =>
        this ! receiveForkRight
      }
      (hungry, done)
    case Hungry(true, true) =>
      println(s"$name got both forks.")
      this ! go
      (Eating, done)
    case Eating =>
      println(s"$name is done eating")
      (Thinking, done)
  }

  def receiveForkLeft = stateHandler {
    case Hungry(false, r) =>
      this ! go
      (Hungry(true, r), done)
    case _ =>
      throw new RuntimeException(s"Bad state when receiving left fork: $name")
  }

  def receiveForkRight = stateHandler {
    case Hungry(l, false) =>
      this ! go
      (Hungry(l, true), done)
    case _ =>
      throw new RuntimeException(s"Bad state when receiving right fork: $name")
  }

}
