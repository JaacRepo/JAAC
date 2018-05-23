package classic.diningphilosophers

import abs.api.cwi.ABSFuture.done
import abs.api.cwi.{ABSFuture, ActorFsm, TypedActor}

class Fork(name: String) extends TypedActor with ActorFsm {
  sealed trait FState extends AbstractState
  case object Free extends FState
  case object Taken extends FState

  override type TState = FState

  override def initState = Free

  def acquire: ABSFuture[Void] = stateHandler {
    case Free =>
      println(s"Picked up $name")
      (Taken, done)
    case Taken =>
      println(s"$name is busy...")
      (Taken, this.acquire)
  }

  def release = stateHandler {
    case Taken =>
      println(s"Put down $name")
      (Free, done)
    case Free =>
      println(s"Releasing a free fork!!! $name")
      (Free, done)
  }

}
