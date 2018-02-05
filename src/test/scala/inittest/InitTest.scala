package inittest

import abs.api.cwi.SugaredActor

class InitTest extends SugaredActor {
  var x = false

  def init() = absVoidMethod {
    x = true
    println("Initted")
  }

  for (_ <- 1 to 1000) {
    Thread.sleep(1)
    if (x)
      throw new RuntimeException("Init method is run before construction completed.")
  }
  println("Finished construction.")
}

object InitTestMain {
  def main(args: Array[String]): Unit = {
    val test = new InitTest
    test.send(() => test.init())
  }
}
