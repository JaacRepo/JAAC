package fibonacchi

import abs.api.cwi.{ABSFuture, SugaredActor, TypedActor}

class FibScalaActor(parent: FibScalaActor) extends SugaredActor with TypedActor[FibScalaActor]{

  private[fibonacchi] var result = 0
  private[fibonacchi] var respReceived = 0
  private[fibonacchi] var t1 = 0L



  def this(parent: FibScalaActor,t1: Long) {
    this(parent)
    this.t1 = t1
  }


  def request(n: Int): Message[Void] = messageHandler{
    if (n <= 2) {
      result = 1
      processResult(1)
    }
    else {
      val f1 = new FibScalaActor(this)
      val f2 = new FibScalaActor(this)
      f1!f1.request(n-1)
      f2!f2.request(n - 2)
    }
    ABSFuture.done
  }

  def response(n: Int): Message[Void] = messageHandler {
    respReceived += 1
    result += n
    if (respReceived == 2) processResult(result)
    ABSFuture.done
  }

  def processResult(n: Int): Unit = {
    if (parent != null)
      parent!parent.response(n)
    else {
      System.out.println("Result= " + result)
      System.out.println(System.currentTimeMillis - t1)
    }
  }
}
