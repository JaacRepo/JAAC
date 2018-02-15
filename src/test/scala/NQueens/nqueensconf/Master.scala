package NQueens.nqueensconf

import java.util.Objects

import abs.api.cwi.ABSFutureSugar.VoidFuture
import abs.api.cwi._


class Master(var numWorkers : Int,var priorities : Int,var solutionsLimit : Int,var threshold : Int,var size : Int) extends SugaredActor with IMaster {

  private val workerSeq = for (_ <- 1 to numWorkers) yield {
    new Worker(this, threshold, size)
  }
  private final val workers = Iterator.continually(workerSeq).flatten

  var result: List[Array[Int]] = List()
  private var resultCounter: Int = 0
  private val t1 = System.currentTimeMillis()

  def success(solution: Array[Int]): VoidFuture = absVoidMethod {
    result = solution +: result
    resultCounter = resultCounter + 1
    if (Objects.equals(resultCounter, solutionsLimit)) {
      println(s"Found ${result.size} solutions")
      println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1))
      ActorSystem.shutdown()
    }
  }

  def sendWork(list: Array[Int], depth: Int, priorities: Int): VoidFuture = absVoidMethod {
//    println(s"Work $depth")
    val worker = workers.next()
    worker.send(() => worker.nqueensKernelPar(list, depth, priorities))
  }

  def init = {
    println(s"NON-CCOP: Boardsize = ${size.toString}, number of solutions should be ${solutionsLimit.toString}")
    val inArray: Array[Int] = new Array[Int](0)
    this.send(() => this.sendWork(inArray, 0, priorities))
  }
}
