package NQueens.confNoLoadBalancing

import abs.api.cwi._

trait IWorker extends TypedActor {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): MessageHandler[Void]
}
