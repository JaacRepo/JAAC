package NQueens.nqueenscoop

import abs.api.cwi._

trait IMaster extends Actor with Ordered[Actor] {
  def sendWork( list : Array[Int],  depth : Int,  priorities : Int): ABSFuture[List[Array[Int]]]
}
