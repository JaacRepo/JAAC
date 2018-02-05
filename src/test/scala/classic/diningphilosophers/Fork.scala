package classic.diningphilosophers

import abs.api.cwi.{ActorFsm, SugaredActor, TypedActor}
import abs.api.cwi.ABSFuture.done

class Fork(name: String) extends SugaredActor with TypedActor[Fork] with ActorFsm[Fork] {
  sealed trait FState extends AbstractState
  case object Free extends FState
  case object Taken extends FState

  override type TState = FState

  override def initState = Free

  def acquire: Message[Void] = stateHandler {
    case Free =>
      println(s"Picked up $name")
      (Taken, done)
    case Taken =>
      println(s"$name is busy...")
      (Taken, this ! acquire)
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
