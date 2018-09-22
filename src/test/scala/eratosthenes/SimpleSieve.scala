package eratosthenes

import com.ascoop.Future.done
import com.ascoop.{Future, TypedActor}

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

object SimpleSieveStart extends TypedActor {
  import com.ascoop.FutureFunctions._

  def start: Future[Void] = messageHandler {
    var t1 = System.currentTimeMillis()
    var two = new SimpleSieve(2)
    var futures = for (i <- 3 to 200) yield {two.divide(i)}
    sequence(futures) onSuccess { resultsIter: Iterable[Option[Int]] =>
      val results = resultsIter.toList
      val primes = 2 +: results.flatten
      println(s"found ${primes.size} primes: in ${System.currentTimeMillis()-t1} " )
      this.start
    }
  }
}

object SimpleSieveMain {
  def main(args: Array[String]): Unit = {
    SimpleSieveStart.start
  }
}