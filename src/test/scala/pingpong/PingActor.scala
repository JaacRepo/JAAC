package pingpong

import abs.api.cwi._
import abs.api.cwi.ABSFuture.done

trait PingInterface extends TypedActor {
  def start(iterations: Int): ABSFuture[Void]
  def pong: ABSFuture[Void]
}

class PingActor(pongActor: PongActor) extends PingInterface {

  var pingsLeft = 0
  var t1 = 0L

  override def start(iterations: Int) = messageHandler {
    t1 = System.currentTimeMillis
    pongActor.ping(this)
    pingsLeft = iterations - 1

    //this part illusrates the use of cooperative scheduling based on an Actor's state, but for benchmarking purposes it is better
    //to use the code in pong, as (pingsLeft==0) will may be checked upon scheduling a new task in this actor, and in this PinPong
    //example we know we can verify this directly in the pong method.

    on (pingsLeft == 0) execute {
      val t2 = System.currentTimeMillis
      pongActor.stop
      println(s"Finished in ${t2-t1} milliseconds")
      done
    }
    //
    done
  }

  private def ping: ABSFuture[Void] = {
    pongActor.ping(this)
    pingsLeft -= 1
    done
  }

  override def pong: ABSFuture[Void] = messageHandler {
    if (pingsLeft > 0)
      this.ping

    //For benchmarking this simple example it is better to use the block of code below to run several iterations, and obtain faster results

    /*else {
      println("Done in " + (System.currentTimeMillis - t1))
      //delete this to run only once
      this.start(100000000);
    }
   */
    done
  }
}
