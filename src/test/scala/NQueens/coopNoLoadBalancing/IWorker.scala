package NQueens.coopNoLoadBalancing

import abs.api.cwi._

trait IWorker extends TypedActor {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): ABSFuture[List[Array[Int]]]
}
