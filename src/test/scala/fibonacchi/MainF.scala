package fibonacchi


object MainF {

  def main(args: Array[String]): Unit = {
    val N = 25
    val fjRunner = new FibScalaActor(null, System.currentTimeMillis);
    val f = fjRunner!fjRunner.request(N)
  }
}
