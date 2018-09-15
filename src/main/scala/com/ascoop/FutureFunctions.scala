package com.ascoop

import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean

object FutureFunctions {

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class FutureImplicit[V](val fut: Future[V]) extends AnyVal {
    def onSuccess[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(fut, continuation)

    def map[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(fut, continuation)

    // TODO: test
    def flatMap[R](continuation: CallableGet[Future[R], V])(implicit hostActor: LocalActor): Future[R] =
      fut.map(continuation).flatten

    // TODO: test
    def flatten[R](implicit ev: V <:< Future[R]): Future[R] = {
      new Future[R] with Actor {
        override def send[T](message: Callable[Future[T]]): Future[T] = {
          fut.getOrNull().asInstanceOf[Future[R]].backLink(this)
          null
        }

        override def spawn[V](guard: Guard, message: Callable[Future[V]]): Future[V] = ???
        override def getSpawn[T, V](f: Future[V], message: CallableGet[T, V], priority: Int, strict: Boolean): Future[T] = ???
      }
    }

    def forEach(continuation: CallableGet[Void, V])(implicit hostActor: LocalActor): Unit =
      hostActor.getSpawn(fut, continuation)

  }

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class FutureBlockingImplicit[V](val fut: Future[V]) extends AnyVal {
    def blockingOnSuccess[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(fut, continuation, Actor.HIGH, Actor.STRICT)
  }

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class FutureIterableImplicit[V](val futList: Iterable[Future[V]]) extends AnyVal {
    def onSuccessAll[R](continuation: CallableGet[R, Iterable[V]])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(sequence(futList), continuation)
  }

  def sequence[R](futures: Iterable[Future[R]]): Future[Iterable[R]] = {
    new Future[Iterable[R]] with Actor {
      // this implements the Actor interface to be able to receive the wake-up message but it does not mean that
      // it is thread-safe by itself. Therefore we use an atomic boolean to ensure safety.
      private val completed = new AtomicBoolean(false)

      override def awaiting(actor: Actor): Unit = {
        super.awaiting(actor)
        futures.foreach(_.awaiting(this))
        this.send(null)
      }

      override def isDone: Boolean = completed.get()

      override def getOrNull(): Iterable[R] = {
        if (completed.get()) {
          futures.map(_.getOrNull())
        } else {
          null
        }
      }

      override def send[V](message: Callable[Future[V]]): Future[V] = {
        completed.compareAndSet(false, futures.forall(_.isDone))
        if (completed.get())
          notifyDependants()
        null
      }

      // the following methods will never be called
      override def complete(value: Iterable[R]): Unit = ???
      override def spawn[V](guard: Guard, message: Callable[Future[V]]): Future[V] = ???
      override def getSpawn[T, V](f: Future[V], message: CallableGet[T, V], priority: Int, strict: Boolean): Future[T] = ???
    }
  }
}
