package abs.api.cwi

import java.lang
import java.util.concurrent.Callable
import java.util.function.Supplier

object TypedActor {
  trait MessageHandler[P <: TypedActor[P], V] extends Callable[ABSFuture[V]]
}

trait ActorFsm[A <: ActorFsm[A] with TypedActor[A]] {
  self: A =>
  import abs.api.cwi.TypedActor.MessageHandler
  trait AbstractState
  type TState <: AbstractState

  def initState: TState

  private var currentState: TState = initState

  def stateHandler[V](fn: TState => (TState, ABSFuture[V])): MessageHandler[A, V] = new MessageHandler[A, V] {
    override def call(): ABSFuture[V] = {
      val (newState, output) = fn(currentState)
      currentState = newState
      output
    }
  }
}

trait TypedActor[A <: TypedActor[A]] extends LocalActor {
  self: A =>
  import TypedActor._
  type Message[V] = MessageHandler[A, V]

  def messageHandler[V](fn: => ABSFuture[V]): MessageHandler[A, V] = new MessageHandler[A, V] {
    override def call() = fn
  }

  def ![V](message: MessageHandler[A, V]): ABSFuture[V] = this.send(message)
}

object SugaredActor {
  // using a value class for less runtime overhead
  class GuardHelper private[SugaredActor] (val g: Guard) extends AnyVal {
    def execute[T](continuation: Callable[ABSFuture[T]])(implicit hostActor: LocalActor): ABSFuture[T] = {
      hostActor.spawn(g, continuation)
    }
  }
}

abstract class SugaredActor extends LocalActor {
  import SugaredActor._

  implicit val hostActor: LocalActor = this

  def absMethod[T](fn: => T): ABSFuture[T] = ABSFuture.done(fn)

  def absVoidMethod(fn: => Unit): ABSFuture[Void] = { fn; ABSFuture.done }

  def absContinuation[T](fn: => T): Callable[ABSFuture[T]] = () => ABSFuture.done(fn)

  def absVoidContinuation(fn: => Unit): Callable[ABSFuture[Void]] = () => { fn; ABSFuture.done }

  def on(guard: => Boolean) = {
    val supplier = new Supplier[java.lang.Boolean] {
      override def get(): lang.Boolean = guard
    }
    new GuardHelper(Guard.convert(supplier))
  }

  def on(guard: Guard) = new GuardHelper(guard)
}

object ABSFutureSugar {
  type VoidFuture = ABSFuture[Void]

  // using a value class for less runtime overhead
  implicit class ABSFutureImplicit[V](val fut: ABSFuture[V]) extends AnyVal {
    def onSuccess[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): ABSFuture[R] =
      hostActor.getSpawn(fut, continuation)
  }

  // using a value class for less runtime overhead
  implicit class ABSFutureIterableImplicit[V](val futList: Iterable[ABSFuture[V]]) extends AnyVal {
    def onSuccessAll[R](continuation: CallableGet[R, List[V]])(implicit hostActor: LocalActor): ABSFuture[R] =
      hostActor.getSpawn(sequence(futList), continuation)
  }

  def sequence[R](futures: Iterable[ABSFuture[R]]): ABSFuture[List[R]] = {
    new ABSFuture[List[R]] with Actor {
      private var completed = false

      override def awaiting(actor: Actor): Unit = {
        super.awaiting(actor)
        futures.foreach(_.awaiting(this))
        this.send(null)
      }

      override def isDone: Boolean = completed

      override def getOrNull(): List[R] = {
        if (completed) {
          futures.map(_.getOrNull()).toList
        } else {
          null
        }
      }

      override def send[V](message: Callable[ABSFuture[V]]): ABSFuture[V] = {
        if (!completed) {
          completed = futures.forall(_.isDone)
        }
        if (completed)
          notifyDependant()
        null
      }

      override def forward(target: ABSFuture[List[R]]): Unit = ???
      override def complete(value: List[R]): Unit = ???
      override def spawn[V](guard: Guard, message: Callable[ABSFuture[V]]) = ???
      override def getSpawn[T, V](f: ABSFuture[V], message: CallableGet[T, V], priority: Int, strict: Boolean) = ???
    }
  }
}
