package abs.api.cwi

import java.lang
import java.util.concurrent.Callable
import java.util.function.Supplier

object TypedActor {
  // using a value class for less runtime overhead
  class GuardHelper private[TypedActor] (val g: Guard) extends AnyVal {
    def execute[V](continuation: => Future[V])(implicit hostActor: LocalActor): Future[V] = {
      hostActor.spawn(g, () => continuation)
    }
  }
}

trait TypedActor extends LocalActor {
  import TypedActor._

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class FutureImplicit[V](val fut: Future[V]) {
    def onSuccess[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(fut, continuation)
  }

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class FutureIterableImplicit[V](val futList: Iterable[Future[V]]) {
    def onSuccessAll[R](continuation: CallableGet[R, List[V]])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(sequence(futList), continuation)
  }

  def sequence[R](futures: Iterable[Future[R]]): Future[List[R]] = {
    new Future[List[R]] with Actor {
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

      override def send[V](message: Callable[Future[V]]): Future[V] = {
        if (!completed) {
          completed = futures.forall(_.isDone)
        }
        if (completed)
          notifyDependant()
        null
      }

      // the following methods will never be called
      override def forward(target: Future[List[R]]): Unit = ???
      override def complete(value: List[R]): Unit = ???
      override def spawn[V](guard: Guard, message: Callable[Future[V]]): Future[V] = ???
      override def getSpawn[T, V](f: Future[V], message: CallableGet[T, V], priority: Int, strict: Boolean): Future[T] = ???
    }
  }

  def messageHandler[V](fn: => Future[V]): Future[V] = {
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