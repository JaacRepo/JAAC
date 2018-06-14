package eratosthenes

import com.ascoop.Future.done
import com.ascoop.{Future, TypedActor}

class Sieve(prime: Long, numMax: Int) extends TypedActor {

  var next: Sieve = null
  var primes = new Array[Long](numMax);
  primes(0) = prime;
  var availableLocalPrimes = 1;


  private def handleNewPrime(newPrime: Long): Unit = {
    if (availableLocalPrimes < numMax) {
      // Store locally if there is space
      primes(availableLocalPrimes) = newPrime
      availableLocalPrimes += 1
    } else {
      next = new Sieve(newPrime, numMax)
    }
    Future.done()
  }
  def longbox(candidate:Long): Future[Void] = messageHandler{
    var isPrime  = FastFunctions.isLocallyPrime(candidate, primes,0,availableLocalPrimes)
    if(isPrime){
      if(next!=null){
        next.longbox(candidate)
      }
      else{
        handleNewPrime(candidate)
      }

    }
    Future.done()
  }

  def exit(m:Int): Future[Void] = messageHandler{
    if (next != null) {
      // Signal next actor for termination
      next.exit(m+availableLocalPrimes)
    } else {
      val totalPrimes = m + availableLocalPrimes
      println("Total primes = " + totalPrimes)

      ///

    }
    Future.done()
  }
}

object SieveMain extends TypedActor {
  import com.ascoop.TypedActor._

  protected var N: Long = 1000000
  protected var M: Int = 2000

  def main(args: Array[String]): Unit = {
    doSieve
  }

  //var it = 0

  private def doSieve: Unit = {
    var t1 = System.currentTimeMillis()
    var three = new Sieve(3, M);
    var i = 5
    while (i < N) {
      three.longbox(i)
      i += 2
    }
    val f = three.longbox(N)
    f onSuccess { results: Void =>
      val res = three.exit(1);
      res.onSuccess { result: Void =>
        println(System.currentTimeMillis()-t1)
        N+=1000000
        if (N <= 10000000)
          doSieve
        done()
      }
    }
  }
}

