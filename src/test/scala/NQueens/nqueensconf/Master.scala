package NQueens.nqueensconf

import java.util.Objects

import com.ascoop.{ActorSystem, Future, TypedActor}


class Master(var numWorkers : Int,var priorities : Int,var solutionsLimit : Int,var threshold : Int,var size : Int) extends TypedActor with IMaster {

  private val workerSeq = for (_ <- 1 to numWorkers) yield {
    new Worker(this, threshold, size)
  }
  private final val workers = Iterator.continually(workerSeq).flatten

  var result: List[Array[Int]] = List()
  private var resultCounter: Int = 0
  private var t1 = System.currentTimeMillis()

  def success(solution: Array[Int]): Future[Void] = messageHandler {
      result = solution +: result
      resultCounter = resultCounter + 1
      if (Objects.equals(resultCounter, solutionsLimit)) {
        println(s"Found ${result.size} solutions")
        println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1))
        val inArray: Array[Int] = new Array[Int](0)
        result= List()
        resultCounter = 0
        t1 = System.currentTimeMillis()

        //delete these 2 lines to run only once
        this.sendWork(inArray, 0, priorities)
        Future.done()
        //ActorSystem.shutdown() uncomment this for termination after setting a fixed number of iterations or after one run.
      }
    else
        Future.done()
    }


  def sendWork(list: Array[Int], depth: Int, priorities: Int): Future[Void] = messageHandler {
//    println(s"Work $depth")
    val worker = workers.next()
    val f =worker.nqueensKernelPar(list, depth, priorities)
    Future.done()
  }

  def init = {
    println(s"NON-COOP: Boardsize = ${size.toString}, number of solutions should be ${solutionsLimit.toString}")
    val inArray: Array[Int] = new Array[Int](0)
    this.sendWork(inArray, 0, priorities)
  }
}
