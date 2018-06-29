object Test {

  implicit def f2int(f: Double):Int = f.round.toInt

  def main(args: Array[String]): Unit = {
    def testM:Double = {
      var x: Int = 0;
      var y: Double = 1.5;
      x + y;
    }

    val y = testM
    println(s"$y")
  }
}
