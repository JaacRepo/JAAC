package eratosthenes

import abs.api.cwi.TypedActor
import abs.api.cwi._

class NumberProducer(limit: Long) extends TypedActor{

  protected var M: Int = 100
  var t1  = System.currentTimeMillis()
  def filterActor(primeFilter: PrimeFilter): Future[Void] = messageHandler{
    var candidate=3
    while(candidate<limit){
      primeFilter.longbox(candidate)
      candidate+=2
    }
    primeFilter.exit(t1)
    //t1 = System.currentTimeMillis()
    //filterActor(new PrimeFilter(1,2,M))
    Future.done()
  }

}
