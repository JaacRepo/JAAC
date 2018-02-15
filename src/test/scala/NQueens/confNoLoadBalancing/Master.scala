package NQueens.confNoLoadBalancing

import abs.api.cwi._


class Master(var numWorkers : Int,var priorities : Int,var solutionsLimit : Int,var threshold : Int,var size : Int) extends SugaredActor with IMaster {
  var result: List[Array[Int]] = List()
  var resultCounter = 0
  private val t1 = System.currentTimeMillis()

  def success(solution: Array[Int]) = messageHandler {
    absVoidMethod {
      result = solution +: result
      resultCounter += 1
      if (solutionsLimit == resultCounter) {
        println(s"Found ${result.size} solutions")
        println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1))
        ActorSystem.shutdown()
      }
    }
  }

  def sendWork(list: Array[Int], depth: Int, priorities: Int) = messageHandler {
      //    println(s"Work $depth")
      val worker = new Worker(this, threshold, size)
      worker ! worker.nqueensKernelPar(list, depth, priorities)
  }

  def init = messageHandler {
    println(s"NON-COOP-NOLB: Boardsize = ${size.toString}, number of solutions should be ${solutionsLimit.toString}")
    val inArray: Array[Int] = new Array[Int](0)
    this ! sendWork(inArray, 0, priorities)
  }
}
