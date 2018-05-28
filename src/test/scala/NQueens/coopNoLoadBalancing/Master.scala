package NQueens.coopNoLoadBalancing

import abs.api.cwi._


class Master(var numWorkers: Int, var priorities: Int, var solutionsLimit: Int, var threshold: Int, var size: Int) extends TypedActor with IMaster {

  private var t1 = System.currentTimeMillis()

  def sendWork(list: Array[Int], depth: Int, priorities: Int): ABSFuture[List[Array[Int]]] = messageHandler {
    //println(s"Work $depth")
    val worker = new Worker(threshold, size)
    worker.nqueensKernelPar(list, depth, priorities)
  }

  def init : ABSFuture[Void] = messageHandler {
    println(s"COOP NO-LB: Boardsize = ${size.toString}, number of solutions should be ${solutionsLimit.toString}")
    val inArray: Array[Int] = new Array[Int](0)
    val f = this.sendWork(inArray, 0, priorities)
    f onSuccess(result => {
      println(s"Found ${result.size} solutions")
      println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1))
      t1=System.currentTimeMillis()

      //delete this to run only once
      this.init
      //

      ABSFuture.done()
    })
      /*(
    })*/
  }
}
