package eratosthenes

import abs.api.cwi.Future.done
import abs.api.cwi._

class SimpleSieve(prime: Int) extends TypedActor {
  var next: Option[SimpleSieve] = None

  def divide(toDivide: Int): Future[Option[Int]] = messageHandler {
    if (toDivide % prime == 0) {
      done(None)  // no prime here
    } else {
      next match {
        case None =>
          next = Some(new SimpleSieve(toDivide))
          done(Some(toDivide))  // found a prime
        case Some(nextPrime) =>
          nextPrime.divide(toDivide)  // delegate
      }
    }
  }
}

object SimpleSieveMain extends TypedActor {
  import TypedActor._

  def main(args: Array[String]): Unit = {
    var t1 = System.currentTimeMillis()
    var two = new SimpleSieve(2)
    var futures = for (i <- 3 to 10000) yield {two.divide(i)}
    sequence(futures) onSuccess { resultsIter: Iterable[Option[Int]] =>
      val results = resultsIter.toList
      val primes = 2 +: results.flatten
      println(s"found ${primes.size} primes: in ${System.currentTimeMillis()-t1} " )
      this.main(null)
      done
    }
  }
}
