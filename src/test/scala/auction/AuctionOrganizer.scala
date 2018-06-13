package auction

import abs.api.cwi.Future.done
import abs.api.cwi.TypedActor
import auction.DataTypes.{Container, Route, Train}

class AuctionOrganizer(trains: List[Train], containers: List[Container]) extends TypedActor {
  import auction.AuctionOrganizer.Result
  import TypedActor._

  def init(timeSlotNow: Double) = messageHandler {
    val trainBidders = trains.map { train =>
      import train._
      new Bidder[Train](availableSince, availableUntil, None, budget, riskFactor, train)
    }

    val goal = Route(timeSlotNow, None)
    val auctioneer = new Auctioneer[Train](Map(), goal, trainBidders, 1)
    val roundOneFuture = auctioneer.start()

    roundOneFuture onSuccess { result: Result[Train] =>
      if (result.winners.isEmpty) {
        done // (empty result)
      } else {
        val containerBidders = containers.map { container =>
          import container._
          new Bidder[Container](arrival, deadline, destination, budget, riskFactor, container)
        }
        val winnerTrain: Train = result.winners.head
        val paidPrice = result.prices.head
        println(s"Winner train is $winnerTrain with price of $paidPrice")
        val auctioneerTrain = new Auctioneer[Container](Map(), goal, containerBidders, winnerTrain.space)
        val roundTwoFuture = auctioneerTrain.start()

        roundTwoFuture onSuccess { result: Result[Container] =>
          println(s"We are going to ${result.winners.head.destination} and profit is ${result.prices.sum - paidPrice}.")
          done  // TODO send results to DSOL?
        }
      }
    }
  }
}

object AuctionOrganizer {
  case class Result[T](winners: List[T], prices: List[Double], unhappy: List[T])
}
