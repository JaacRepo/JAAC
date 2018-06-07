package eratosthenes

import abs.api.cwi.TypedActor
import abs.api.cwi._

class PrimeFilter(val id: Int, val myInitialPrime: Long, numMaxLocalPrimes: Int) extends TypedActor{

  var nextFilterActor: PrimeFilter = null
  val localPrimes = new Array[Long](numMaxLocalPrimes)

  var availableLocalPrimes = 1
  localPrimes(0) = myInitialPrime

  private def handleNewPrime(newPrime: Long): Unit = {
    if (availableLocalPrimes < numMaxLocalPrimes) {
      // Store locally if there is space
      localPrimes(availableLocalPrimes) = newPrime
      availableLocalPrimes += 1
    } else {
      nextFilterActor = new PrimeFilter(id + 1, newPrime, numMaxLocalPrimes)
    }
    Future.done()
  }

  def longbox(candidate:Long): Future[Void] = messageHandler{
    var isPrime  = FastFunctions.isLocallyPrime(candidate, localPrimes,0,availableLocalPrimes)
    if(isPrime){
      if(nextFilterActor!=null){
        nextFilterActor.longbox(candidate)
      }
      else{
        handleNewPrime(candidate)
      }

    }
    Future.done()
  }

  def exit(m:Long): Future[Void] = messageHandler{
    if (nextFilterActor != null) {
      // Signal next actor for termination
      nextFilterActor.exit(m)
    } else {
      val totalPrimes = ((id - 1) * numMaxLocalPrimes) + availableLocalPrimes
      println("Total primes = " + totalPrimes+ " in "+(System.currentTimeMillis()-m))
      var N: Long = 1000000
      var M: Int = 2000

      //delete this part to have only one run
      val np  = new NumberProducer(N)
      val pf = new PrimeFilter(1,2,M)
      np.filterActor(pf)
      ///

    }
    Future.done()
  }
}
