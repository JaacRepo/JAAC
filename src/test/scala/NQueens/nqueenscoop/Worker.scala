package NQueens.nqueenscoop

import NQueens.common.FastFunctions
import abs.api.cwi.ABSFuture.done
import abs.api.cwi.ABSFutureSugar._
import abs.api.cwi._


class Worker(var master: IMaster, var threshold: Int, var size: Int) extends SugaredActor with IWorker {
  {
    println("Worker started")
  }

  def nqueensKernelPar(board: Array[Int], depth: Int, priority: Int): ABSFuture[List[Array[Int]]] = {
//    println(s"Par $depth $size $priority ${board.length} $this")
    if (size != depth) {
      if (depth >= threshold) {
        done(this.nqueensKernelSeq(board, depth).toList)
      } else {
        val newDepth: Int = depth + 1
        var i: Int = 0
        var futures = List[ABSFuture[List[Array[Int]]]]()
        while (i < size) {
          val b: Array[Int] = new Array[Int](newDepth)
          System.arraycopy(board, 0, b, 0, depth)
          b(depth) = i
          if (FastFunctions.boardValid(b,newDepth)) {
            val fut: ABSFuture[List[Array[Int]]] = master.send(() => master.sendWork(b, newDepth, priority - 1))
            futures = fut +: futures
            this.spawn(fut,  ()=>{
              println("Future f has completed")
              println("this actor will run this continuation on the same thread")
              done();
            });
          }
          i += 1
        }

        sequence(futures).onSuccess(list => absMethod {list.flatten})
      }
    } else {
      done(List(board))

    }
  }

  def nqueensKernelSeq(board: Array[Int], depth: Int): Vector[Array[Int]] = {
//    println(s"Seq $depth $this")
    if (size != depth) {
      var result = Vector[Array[Int]]()
      val b: Array[Int] = new Array[Int](depth + 1)

      var i: Int = 0
      while (i < size) {
        System.arraycopy(board, 0, b, 0, depth)
        b(depth) = i
        if (FastFunctions.boardValid(b, depth + 1)) {
          result = nqueensKernelSeq(b, depth + 1) ++ result
        }
        i += 1
      }
      result
    } else {
//      println("Solution "+this)
      Vector(board) // solution
    }
  }
}
