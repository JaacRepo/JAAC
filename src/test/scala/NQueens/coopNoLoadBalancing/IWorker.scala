package NQueens.coopNoLoadBalancing

import abs.api.cwi._

trait IWorker extends TypedActor[IWorker] {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): Message[List[Array[Int]]]
}
