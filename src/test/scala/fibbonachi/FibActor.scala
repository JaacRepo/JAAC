package fibbonachi

import abs.api.cwi.{ABSFuture, TypedActor}

class FibActor(parent: FibActor) extends TypedActor{
  private var result = 0
  private var respReceived = 0
  private var t1 = 0L




  def this(parent: FibActor,t1: Long) {
    this(parent)
    this.t1 = t1
  }


  def request(n: Int): ABSFuture[Void] = messageHandler{
    //if(parent==null)
    //System.out.println("Start "+n)
    if (n <= 2) {
      result = 1
      processResult(1)
    }
    else {
      val f1 = new FibActor(this)
      val f2 = new FibActor(this)
      f1.request(n - 1)
      f2.request(n - 2)
    }
    ABSFuture.done()
  }

  def response(n: Int): ABSFuture[Void] = messageHandler {
    respReceived += 1
    result += n
    if (respReceived == 2) processResult(result)
    ABSFuture.done
  }

  def processResult(n: Int): Unit = {
    if (parent != null)
      parent.response(n)
    else {
      System.out.println("Result= " + result)
      System.out.println(System.currentTimeMillis - t1)
      t1=System.currentTimeMillis()
      result=0
      respReceived=0
      this.request(25)
    }
  }

}
