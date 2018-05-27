package NQueens.coopNoLoadBalancing

import abs.api.cwi._

trait IMaster extends TypedActor {
  def sendWork(list : Array[Int],  depth : Int,  priorities : Int): ABSFuture[List[Array[Int]]]
}
