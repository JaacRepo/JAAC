package eratosthenes

import com.ascoop.{Future, TypedActor}

class NumberProducer(limit: Long) extends TypedActor{

  var t1  = System.currentTimeMillis()
  def filterActor(primeFilter: PrimeFilter): Future[Void] = messageHandler{
    var candidate=3
    while(candidate<limit){
      primeFilter.longbox(candidate)
      candidate+=2
    }
    primeFilter.exit(t1,limit)
    //t1 = System.currentTimeMillis()
    //filterActor(new PrimeFilter(1,2,M))
    Future.done()
  }

}
