package NQueens.nqueenscoop

import abs.api.cwi.ABSFutureSugar._
import abs.api.cwi._


class Master(var numWorkers : Int,var priorities : Int,var solutionsLimit : Int,var threshold : Int,var size : Int) extends SugaredActor with IMaster {

  private val workerSeq = for (_ <- 1 to numWorkers) yield {
    new Worker(this, threshold, size)
  }
  private final val workers = Iterator.continually(workerSeq).flatten

  private val t1 = System.currentTimeMillis()

  def sendWork(list: Array[Int], depth: Int, priorities: Int): ABSFuture[List[Array[Int]]] = {
//    println(s"Work $depth")
    val worker = workers.next()
    worker.send(() => worker.nqueensKernelPar(list, depth, priorities))
  }

  def init: ABSFuture[Void] = {
    println(s"COOP: Boardsize = ${size.toString}, number of solutions should be ${solutionsLimit.toString}")
    val inArray: Array[Int] = new Array[Int](0)
    this.send(() => this.sendWork(inArray, 0, priorities)) onSuccess (result => absVoidMethod {
      println(s"Found ${result.size} solutions")
      println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1))
      ActorSystem.shutdown()
    })
  }
}
