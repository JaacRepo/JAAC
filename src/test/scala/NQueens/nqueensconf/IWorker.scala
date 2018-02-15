package NQueens.nqueensconf

import abs.api.cwi.ABSFutureSugar.VoidFuture
import abs.api.cwi._

trait IWorker extends Actor with Ordered[Actor] {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): VoidFuture
}
