package NQueens.coopNoLoadBalancing

import com.ascoop.{Future, TypedActor}

trait IWorker extends TypedActor {
  def nqueensKernelPar( list : Array[Int],  depth : Int,  priority : Int): Future[Iterable[Array[Int]]]
}
