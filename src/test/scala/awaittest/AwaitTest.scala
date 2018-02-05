package awaittest

import abs.api.cwi.{SugaredActor, TypedActor}

class AwaitTest extends SugaredActor with TypedActor[AwaitTest] {
  var x = false
  var y = false

  def m1() = messageHandler { absVoidMethod {
    x = true
    // when `x` becomes false, the following continuation will be enabled for execution
    on {!x} execute absContinuation {
      y = true
      for (_ <- 1 to 1000) {
        Thread.sleep(1)
        if (! y)
          throw new RuntimeException("Actor has two parallel threads!")
      }
      println("Finished checking.")
    }
  }}

  def m2() = messageHandler { absVoidMethod {
    y = false
    // when `x` becomes true, the following continuation will be enabled for execution
    on {x} execute absContinuation {
      x = false
      // when `y` becomes true, the following continuation will be enabled for execution
      on {y} execute absContinuation {
        y = false
      }
    }
  }}
}

object AwaitTestMain {
  def main(args: Array[String]): Unit = {
    val a = new AwaitTest
    import a._

    a ! m1()
    a ! m2()
  }
}
