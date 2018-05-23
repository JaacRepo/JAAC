package abs.api.cwi

import java.lang
import java.util.concurrent.Callable
import java.util.function.Supplier

object TypedActor {
  // using a value class for less runtime overhead
  class GuardHelper private[TypedActor] (val g: Guard) extends AnyVal {
    def execute[V](continuation: => ABSFuture[V])(implicit hostActor: LocalActor): ABSFuture[V] = {
      hostActor.spawn(g, () => continuation)
    }
  }
}

trait TypedActor extends LocalActor {
  import TypedActor._

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class ABSFutureImplicit[V](val fut: ABSFuture[V]) {
    def onSuccess[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): ABSFuture[R] =
      hostActor.getSpawn(fut, continuation)
  }

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class ABSFutureIterableImplicit[V](val futList: Iterable[ABSFuture[V]]) {
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

      // the following methods will never be called
      override def forward(target: ABSFuture[List[R]]): Unit = ???
      override def complete(value: List[R]): Unit = ???
      override def spawn[V](guard: Guard, message: Callable[ABSFuture[V]]): ABSFuture[V] = ???
      override def getSpawn[T, V](f: ABSFuture[V], message: CallableGet[T, V], priority: Int, strict: Boolean): ABSFuture[T] = ???
    }
  }

  def messageHandler[V](fn: => ABSFuture[V]): ABSFuture[V] = {
    hostActor.send(() => fn)
  }

  implicit val hostActor: LocalActor = this

  def on(guard: => Boolean) = {
    val supplier = new Supplier[java.lang.Boolean] {
      override def get(): lang.Boolean = guard
    }
    new GuardHelper(Guard.convert(supplier))
  }

  def on(guard: Guard) = new GuardHelper(guard)
}