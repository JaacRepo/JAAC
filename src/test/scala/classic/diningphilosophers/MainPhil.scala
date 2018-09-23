package classic.diningphilosophers

import com.ascoop.{ActorSystem, TypedActor}

object MainPhil extends TypedActor {
  def main(args: Array[String]): Unit = {
    val n = 5
    val forks = (0 until n).map (i => new Fork(s"fork $i"))
    val philosophers = (0 until n).map (i => new Philosopher(s"phil $i", forks(i), forks((i+1) % n))).foreach(_.go)

    Thread.sleep(100)

    ActorSystem.shutdown()
  }
}
