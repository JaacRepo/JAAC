package classic.diningphilosophers

import com.ascoop.Future.done
import com.ascoop.{ActorFsm, Future, TypedActor}

sealed trait FState
case object Free extends FState
case object Taken extends FState

class Fork(name: String) extends TypedActor with ActorFsm {
  override type TState = FState

  override def initState = Free

  def acquire: Future[Void] = stateHandler {
    case Free =>
      println(s"Picked up $name")
      goto(Taken) andReturn done
    case Taken =>
//      println(s"$name is busy...")  // TODO ideally shouldn't do a busy loop here
      goto(Taken) andReturn this.acquire
  }

  def release = stateHandler {
    case Taken =>
      println(s"Put down $name")
      goto(Free) andReturn done
    case Free =>
      println(s"Releasing a free fork!!! $name")
      goto(Free) andReturn done
  }
}
