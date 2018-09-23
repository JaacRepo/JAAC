package com.ascoop

import java.util.concurrent.atomic.AtomicInteger

object FutureFunctions {
  // TODO: filter, traverse, fold, reduce, fallbackTo, zip, etc.

  // to use a value class for less runtime overhead, it must be in companion object, but then it will need to be explicitly imported in actors
  implicit class FutureImplicit[V](val fut: Future[V]) extends AnyVal {
    def onSuccess[R](continuation: CallableGet[R, V])(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(fut, continuation)

    def map[R](mapping: V => R)(implicit hostActor: LocalActor): Future[R] =
      hostActor.getSpawn(fut, (v: V) => Future.done(mapping(v)))

    def flatMap[R](mapping: V => Future[R])(implicit hostActor: LocalActor): Future[R] = {
      hostActor.getSpawn(fut, (v: V) => mapping(v))
    }

    def flatten[R](implicit ev: V <:< Future[R], hostActor: LocalActor): Future[R] = hostActor.getSpawn(fut, (innerFut: V) => innerFut)  // TODO has the same backlink issue
//    def flatten[R](implicit ev: V <:< Future[R]): Future[R] = {
//      val newFuture = new Future[R] with Actor {
//        override def send[T](message: Callable[Future[T]]): Future[T] = {
//          fut.getOrNull().asInstanceOf[Future[R]].backLink(this) // TODO: that future have another backlink already, e.g., call flatten twice on same variable
//          null
//        }
//
//        override def spawn[T](guard: Guard, message: Callable[Future[T]]): Future[T] = ???
//
//        override def getSpawn[R, T](f: Future[T],
//                                    message: CallableGet[R, T],
//                                    priority: Int,
//                                    strict: Boolean): Future[R] = ???
//      }
//      fut.awaiting(newFuture)
//      newFuture
//    }

    def forEach(continuation: V => Unit)(implicit hostActor: LocalActor): Unit =
      fut.map(continuation)

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

  def sequence[R](futures: Iterable[Future[R]]): Future[Iterable[R]] = new Future[Iterable[R]] {
    val remaining = new AtomicInteger(futures.size)

    for (fut <- futures) {
      val awaitingGuard = new FutureGuard[R](fut, null) {
        override private[ascoop] def notifyDependants(): Unit = {
          if (remaining.decrementAndGet() == 0)
            complete(futures.map(_.getOrNull))
        }
      }
      if (fut.isDone(awaitingGuard)) {
        awaitingGuard.notifyDependants()
      }
    }
  }
}
