package fibbonachi

import abs.api.cwi.{Future, ActorSystem, TypedActor}

class FibActor(parent: FibActor) extends TypedActor{
  private var result = 0
  private var respReceived = 0
  private var t1 = 0L
  private var iter = 10
  private var N : Int = 0


  def this(parent: FibActor,t1: Long) {
    this(parent)
    this.t1 = t1
  }


  def request(n: Int): Future[Void] = messageHandler{
    //if(parent==null)
    //System.out.println("Start "+n)
    N=n
    if (n <= 2) {
      result = 1
      processResult(1)
    }
    else {
      val f1 = new FibActor(this)
      f1.request(n - 1)
      val f2 = new FibActor(this)
      f2.request(n - 2)
    }
    Future.done()
  }

  def response(n: Int): Future[Void] = messageHandler {
    respReceived += 1
    result += n
    if (respReceived == 2) processResult(result)
    Future.done
  }

  def processResult(n: Int): Unit = {
    if (parent != null)
      parent.response(n)
    else {
      System.out.println("Result= " + result)
      System.out.println(System.currentTimeMillis - t1)
      t1=System.currentTimeMillis()

      //delete this part to run only once
      result=0
      respReceived=0
      iter= iter-1
      if(iter>0) {
        this.request(this.N)
      }
      else
        ActorSystem.shutdown()
      //
    }
  }

}
