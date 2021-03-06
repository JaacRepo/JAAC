package NQueens.coopNoLoadBalancing

import NQueens.common.FastFunctions
import com.ascoop.Future.done
import com.ascoop.FutureFunctions._
import com.ascoop.{Future, TypedActor}

class Worker(var threshold: Int, var size: Int) extends TypedActor with IWorker {

  // This is not a message handler and is called synchronously
  private def sendWork(list: Array[Int], depth: Int, priorities: Int): Future[Iterable[Array[Int]]] = {
    //println(s"Work $depth $this")
    val worker = new Worker(threshold, size)
    worker.nqueensKernelPar(list, depth, priorities)
  }

  def nqueensKernelPar(board: Array[Int], depth: Int, priority: Int): Future[Iterable[Array[Int]]] = messageHandler {
    //println(s"Par $depth $size $priority ${board.length} $this")
    if (size != depth) {
      if (depth >= threshold) {
        done(this.nqueensKernelSeq(board, depth).toList)
      } else {
        val newDepth: Int = depth + 1
        var i: Int = 0
        var futures = List[Future[Iterable[Array[Int]]]]()
        while (i < size) {
          val b: Array[Int] = new Array[Int](newDepth)
          System.arraycopy(board, 0, b, 0, depth)
          b(depth) = i
          if (FastFunctions.boardValid(b, newDepth)) {
            val fut = this.sendWork(b, newDepth, priority - 1)
            futures = fut +: futures
          }
          i += 1
        }
        futures onSuccessAll (list => {
          done(list.flatten)
        })

      }
    } else {
      done(List(board))
    }
  }

  private def nqueensKernelSeq(board: Array[Int], depth: Int): Vector[Array[Int]] = {
//    println(s"Seq $depth $this")
    if (size == depth) {
      Vector(board)
    } else {
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
    }
  }
}
