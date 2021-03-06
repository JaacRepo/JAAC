package auction

import auction.DataTypes.{Container, Train}
import com.ascoop.FutureFunctions._
import com.ascoop.{ActorSystem, Future, TypedActor}

class DSOL extends TypedActor {

  def done(timeSlot: Double,
           dest: Destination,
           tr: AuctioneerInfo,
           winnerContainers: List[BiddingInfo],
           unhappyTrains: List[BiddingInfo],
           unhappyContainers: List[BiddingInfo]): Future[Void] = {
    println(s"done: \nTimeslot: $timeSlot \ndest: $dest \ntr: $tr \nwinners: $winnerContainers \nunhappy: $unhappyTrains $unhappyContainers")
    Future.done()
  }
}

object DsolMain extends TypedActor {

  def main(args: Array[String]): Unit = {
    val nTrains = 10
    val nContainers = 100
    val Munich = Destination("Munich", 10, 100)
    val Duisburg = Destination("Duisburg", 7, 70)
    val trains = (1 to nTrains) map { i =>
      Train(
        space = 10,
        availableSince = (i + 5) / 5,
        availableUntil = 2 * i + 1,
        budget = 1000 * Math.random() + 5 * i,
        riskFactor = i * 0.05 + Math.random()
      )
    }
    val containers = (1 to nContainers) map { i =>
      Container(
        arrival = (i + 1) / 20,
        deadline = (2 * i) / 10,
        destination = if (Math.random() > .5) Some(Munich) else Some(Duisburg),
        budget = 100 * Math.random() + 5 * 10,
        riskFactor = i * 0.05 + Math.random()
      )
    }
    val organizer = new AuctionOrganizer(trains.toList, containers.toList)
    val auctionFuture = organizer.init(5)
    auctionFuture onSuccess { _ =>
      ActorSystem.shutdown()
      Future.done
    }
  }
}
