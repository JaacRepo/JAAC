package eratosthenes

import abs.api.cwi.Future.done
import abs.api.cwi._

class Sieve(prime: Int) extends TypedActor {
  var next: Option[Sieve] = None

  def divide(toDivide: Int): Future[Option[Int]] = messageHandler {
    if (toDivide % prime == 0) {
      done(None)  // no prime here
    } else {
      next match {
        case None =>
          next = Some(new Sieve(toDivide))
          done(Some(toDivide))  // found a prime 
        case Some(nextPrime) =>
          nextPrime.divide(toDivide)  // delegate
      }
    }
  }
}

object SieveMain extends TypedActor {
  def main(args: Array[String]): Unit = {
    var t1 = System.currentTimeMillis()
    var two = new Sieve(2)
    var futures = for (i <- 3 to 10000) yield {two.divide(i)}
    sequence(futures) onSuccess { results: List[Option[Int]] =>
      val primes = 2 +: results.flatten
      println(s"found ${primes.size} primes: in ${System.currentTimeMillis()-t1} " )
      this.main(null)
      done
    }
  }
}

