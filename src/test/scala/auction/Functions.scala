package auction

import auction.DataTypes.{Container, Price, Route}


case class Destination(name: String,
                       drivingTime: Double,
                       costToDestination: Double)

case class AuctioneerInfo(numberOfWinners: Int)
case class BiddingInfo(id: String,
                       goal: Package,
                       budget: Price,
                       arrivalTime: Double,
                       deadline: Double,
                       paidPrice: Option[Price] = None,
                       auctioneer: AuctioneerInfo)

object DataTypes {
  case class Train(space: Int,
                   availableSince: Double,
                   availableUntil: Double,
                   budget: Double,
                   riskFactor: Double)
  case class Container(arrival: Double,
                       deadline: Double,
                       destination: Option[Destination],
                       budget: Double,
                       riskFactor: Double)
  case class Route(departure: Double, city: Option[Destination])

  type Price = Double
  type Belief = Price
  type BeliefBase = Map[Route, Price]
  type GoalBase = Set[Container]
}


object Functions {

  /** a number that says how much the bidder is interested: if the bidder has time then it bids less
    * RESULT RANGE: (0,1] , 1 most interested
    */
  def fit(arrival: Double, deadline: Double, destination: Option[Destination], train: Route): Double = {
    val time = train.departure
    val dest = train.city
    if (deadline < time) return -1.0 // unhappy
    val isDestinationUninteresting = destination.isDefined && dest.isDefined && dest.get != destination.get
    val uninteresting = isDestinationUninteresting || time < arrival
    if (uninteresting)
      0
    else
      (time - arrival + 1) / (deadline - arrival + 1)
  }

  /** risk and timeFit must be between 0..1 so that bid stays between min and budget
    * risk is a parameter of every bidderagent given by DSOL
    */
  def bidStrategy(min: Price,
                  budget: Price,
                  timeFit: Double,
                  risk: Double): Price = {
    if (budget < min)
      0
    else if (timeFit <= 0)
      timeFit // means either not interested or unhappy
    else
      min + (budget - min) * timeFit * risk
  }
}
