package auction

import abs.api.cwi.{ABSFuture, SugaredActor, TypedActor}
import auction.DataTypes._
import abs.api.cwi.ABSFuture.done
import abs.api.cwi.ABSFutureSugar._

import scala.collection.immutable.SortedSet


// init_value and init_gaol must be about the same route.
// NOTE: each bidding agent has to have a comparable unique identifier for reproducibility.
class Auctioneer[BidderType](itemValues: BeliefBase, init_goal: Route, bidders: List[Bidder[BidderType]], numWinners: Int)
    extends SugaredActor
    with TypedActor[Auctioneer[BidderType]] {

  private case class BidNode(bidder: Bidder[BidderType], price: Price)
  implicit private val ordering: Ordering[BidNode] = Ordering.by(node => (node.price, node.bidder.hashCode())) // ordering of bidders is not important, but to make sure they are different

  private case class VickeryAuction(top: SortedSet[BidNode] = SortedSet.empty[BidNode]) {
    def addBid(caller: Bidder[BidderType], bid: Price): VickeryAuction = {
      val topLocal = top + BidNode(caller, bid)
      if (topLocal.size > numWinners + 1) // we keep one more bid for the property of Vickery auctions
        VickeryAuction(topLocal - top.min)
      else
        VickeryAuction(topLocal)
    }

    private def topMinMin = if (top.size > numWinners) top - top.min else top
    private def topMinMax = if (top.size > numWinners) top - top.max else top
    def value: Price = topMinMin.map(_.price).sum
    def winners: List[Bidder[BidderType]] = topMinMin.map(_.bidder).toList
    def prices: List[Price] = topMinMin.map(_.price).toList
  }

  private var winners: Map[Route, VickeryAuction] = Map()
  private var unhappy = List[BidderType]()

  def bid(caller: Bidder[BidderType], toBuy: Route, bid: Price, info: BidderType): Message[Void] =
    messageHandler {
      if (bid > 0) {
        val auctionWinners = winners.getOrElse(toBuy, VickeryAuction())
        winners = winners + (toBuy -> auctionWinners.addBid(caller, bid))
      } else if (bid < 0) {
        unhappy = unhappy :+ info
      }
      done
    }

  def start(): Message[AuctionOrganizer.Result[BidderType]] = messageHandler {
    println("Starting an auction")
    val futures = bidders.map(bidder => bidder ! bidder.announce(this, init_goal, itemValues.getOrElse(init_goal, 0)))
    sequence(futures) onSuccess
      (_ => {
         println("All bids are received")
         val winner: Option[(Route, Price)] = winners.map {
           case (r: Route, auction: VickeryAuction) =>
             val cost = r.city.map((dest) => dest.costToDestination).getOrElse(0.0)
             val bidSum = auction.top.map(_.price).sum
             r -> (bidSum - cost)
         }.reduceOption((bid1, bid2) => if (bid1._2 > bid2._2) bid1 else bid2)

         winner.fold {
           done(AuctionOrganizer.Result(List.empty, List.empty, unhappy))
         } {
           case (route, _) =>
             val auction = winners(route)
             val winnerBidders = auction.winners
             val infoFutures: List[ABSFuture[BidderType]] = winnerBidders.map { bidder =>
               bidder ! bidder.sold(route)
             }
             sequence(infoFutures) onSuccess { winnerInfo: List[BidderType] =>
               done(AuctionOrganizer.Result(winnerInfo, auction.prices, unhappy))
             }
         }
       })
  }
}
