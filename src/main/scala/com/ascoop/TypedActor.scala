package com.ascoop

import java.lang
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Supplier

object TypedActor {
  // using a value class for less runtime overhead
  class GuardHelper private[TypedActor](val g: Guard) extends AnyVal {
    def execute[V](continuation: => Future[V])(implicit hostActor: LocalActor): Future[V] = {
      hostActor.spawn(g, () => continuation)
    }
  }
}

trait TypedActor extends LocalActor {
  import TypedActor._

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