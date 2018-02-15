package NQueens.confNoLoadBalancing

import abs.api.cwi._

trait IMaster extends TypedActor {
  def sendWork(list : Array[Int],  depth : Int,  priorities : Int): MessageHandler[Void]
  def success(solution: Array[Int]): MessageHandler[Void]
}
