package eratosthenes

import abs.api.cwi.ABSFuture.done
import abs.api.cwi.{ActorSystem, SugaredActor, TypedActor}
import abs.api.cwi.ABSFutureSugar._


class Sieve(prime: Int) extends SugaredActor with TypedActor[Sieve] {
  var next: Option[Sieve] = None

  def divide(toDivide: Int): Message[(Int, Boolean)] = messageHandler {
    if (toDivide % prime == 0) {
      done(toDivide -> false)
    } else {
      next match {
        case None =>
          next = Some(new Sieve(toDivide))
          done(toDivide -> true)
        case Some(nextPrime) =>
          nextPrime ! nextPrime.divide(toDivide)
      }
    }
  }
}


object SieveMain extends SugaredActor {
  def main(args: Array[String]): Unit = {
    val two = new Sieve(2)
    import two.divide
    val futures = for (i <- 3 to 1000) yield {two ! divide(i)}
    sequence(futures) onSuccess { results: List[(Int, Boolean)] =>
      val primes = 2 +: results.filter(_._2).map(_._1)
      println(s"found ${primes.size} primes: " + primes.mkString("[", ", ", "]"))
      ActorSystem.shutdown()
      done
    }
  }
}
