package NQueens.nqueensconf

import abs.api.cwi.ABSFutureSugar.VoidFuture
import abs.api.cwi._

trait IMaster extends Actor with Ordered[Actor] {
  def sendWork( list : Array[Int],  depth : Int,  priorities : Int): VoidFuture
  def success(solution: Array[Int]): VoidFuture
}
