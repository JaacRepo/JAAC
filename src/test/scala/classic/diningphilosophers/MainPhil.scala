package classic.diningphilosophers

import abs.api.cwi.{ActorSystem, SugaredActor}

object MainPhil extends SugaredActor {
  def main(args: Array[String]): Unit = {
    val n = 5
    val forks = (0 until n).map (i => new Fork(s"fork $i"))
    val philosophers = (0 until n).map (i => new Philosopher(s"phil $i", forks(i), forks((i+1) % n))).foreach(phil => phil ! phil.go)

    Thread.sleep(100)

    ActorSystem.shutdown()
  }
}
