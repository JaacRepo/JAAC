package typedactor

import abs.api.cwi._
import Future.done


class Example2 extends TypedActor {
  def doubler(a: Int): Future[Int] = messageHandler {
    done(a * 2)
  }
}

class Example3 extends TypedActor {
  def tripler(a: Int): Future[Int] = messageHandler {
    done(a * 3)
  }

}


class User extends TypedActor {
  def call(): Future[Void] = messageHandler {
    val a2 = new Example2
    val a3 = new Example3

    def xxx = a2.messageHandler{
      done
    }

    a2.doubler(2)
    a3.tripler(2)
    //      a2.tripler(2)  // must fail to compile
    done
  }
}
