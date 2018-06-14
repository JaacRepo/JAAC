package NQueens.nqueensconf

import com.ascoop.{Actor, Future}

trait IWorker extends Actor with Ordered[Actor] {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): Future[Void]
}
