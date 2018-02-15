package NQueens.nqueenscoop

import abs.api.cwi._

trait IWorker extends Actor with Ordered[Actor] {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): ABSFuture[List[Array[Int]]]
}
