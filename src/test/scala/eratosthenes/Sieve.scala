package eratosthenes

import abs.api.cwi.ABSFuture.done
import abs.api.cwi._

class Sieve(prime: Int) extends TypedActor {
  var next: Option[Sieve] = None

  def divide(toDivide: Int): ABSFuture[Option[Int]] = messageHandler {
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
  import TypedActor._
  def main(args: Array[String]): Unit = {
    val two = new Sieve(2)
    val futures = for (i <- 3 to 1000) yield {two.divide(i)}
    sequence(futures) onSuccess { results: List[Option[Int]] =>
      val primes = 2 +: results.flatten
      println(s"found ${primes.size} primes: " + primes.mkString("[", ", ", "]"))
      ActorSystem.shutdown()
      done
    }
  }
}

