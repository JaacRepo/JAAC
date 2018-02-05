package NQueens.coopNoLoadBalancing

import abs.api.cwi._
import abs.api.cwi.ABSFuture.done
import abs.api.cwi.ABSFutureSugar._


class Master(var numWorkers: Int, var priorities: Int, var solutionsLimit: Int, var threshold: Int, var size: Int) extends SugaredActor with IMaster {

  private val t1 = System.currentTimeMillis()

  def sendWork(list: Array[Int], depth: Int, priorities: Int) = messageHandler {
    println(s"Work $depth")
    val worker = new Worker(threshold, size)
    worker ! worker.nqueensKernelPar(list, depth, priorities)
  }

  def init = messageHandler {
    println(s"COOP NO-LB: Boardsize = ${size.toString}, number of solutions should be ${solutionsLimit.toString}")
    val inArray: Array[Int] = new Array[Int](0)
    this ! sendWork(inArray, 0, priorities) onSuccess (result => absVoidMethod {
      println(s"Found ${result.size} solutions")
      println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1))
      ActorSystem.shutdown()
    })
  }
}
