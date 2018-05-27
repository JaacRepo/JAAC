package fibbonachi

object FibMain {
  def main(args: Array[String]): Unit = {
    val N = 25
    val fjRunner = new FibActor(null, System.currentTimeMillis);
    val f = fjRunner.request(N)
  }
}
