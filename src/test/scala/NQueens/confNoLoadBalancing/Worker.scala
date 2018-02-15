package NQueens.confNoLoadBalancing

import NQueens.common.FastFunctions
import abs.api.cwi.ABSFuture.done
import abs.api.cwi._

class Worker(val master: IMaster, var threshold: Int, var size: Int) extends LocalActor with IWorker {

  private def sendWork(list: Array[Int], depth: Int, priorities: Int): Unit = {
    //    println(s"Work $depth")
    val worker = new Worker(master, threshold, size)
    worker ! worker.nqueensKernelPar(list, depth, priorities)
  }

  def nqueensKernelPar(board: Array[Int], depth: Int, priority: Int) = messageHandler {
    //    println(s"Par $depth $size $priority ${board.length}")
    if (size != depth) {
      if (depth >= threshold) {
        this.nqueensKernelSeq(board, depth)
      } else {
        val newDepth: Int = depth + 1

        var i: Int = 0
        while (i < size) {
          val b: Array[Int] = new Array[Int](newDepth)
          System.arraycopy(board, 0, b, 0, depth)
          b(depth) = i
          if (FastFunctions.boardValid(b, newDepth)) {
            this.sendWork(b, newDepth, priority - 1)
          }
          i += 1
        }
      }
    } else {
      println("solution")
      board.foreach(print)
      master ! master.success(board)
    }
    done
  }

  // internal method called only sequentially
  private def nqueensKernelSeq(board: Array[Int], depth: Int): Unit = {
    //    println(s"Seq $depth")
    if (size != depth) {
      val b: Array[Int] = new Array[Int](depth + 1)

      var i: Int = 0
      while (i < size) {
        System.arraycopy(board, 0, b, 0, depth)
        b(depth) = i
        if (FastFunctions.boardValid(b, depth + 1)) {
          nqueensKernelSeq(b, depth + 1)
        }
        i += 1
      }
    } else {
      master ! master.success(board)
    }
    //    println(s"Seq $depth is done")
  }
}
