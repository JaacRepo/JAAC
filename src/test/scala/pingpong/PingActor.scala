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
    on (pingsLeft == 0) execute {
      println("Done in " + (System.currentTimeMillis - t1))

      //delete this to run only once
      this.start(100000)
      //
    }
  }

  private def ping: ABSFuture[Void] = {
    pongActor.ping(this)
    pingsLeft -= 1
    done
  }

  override def pong: ABSFuture[Void] = messageHandler {
    if (pingsLeft > 0)
      this.ping
    done
  }
}
