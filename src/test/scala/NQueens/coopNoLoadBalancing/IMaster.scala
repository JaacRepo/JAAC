package NQueens.coopNoLoadBalancing

import com.ascoop.{Future, TypedActor}

trait IMaster extends TypedActor {
  def sendWork(list : Array[Int],  depth : Int,  priorities : Int): Future[Iterable[Array[Int]]]
}
