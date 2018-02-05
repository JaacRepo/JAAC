package NQueens.coopNoLoadBalancing

import abs.api.cwi._

trait IMaster extends TypedActor[IMaster] {
  def sendWork(list : Array[Int],  depth : Int,  priorities : Int): Message[List[Array[Int]]]
}
