package pingpong

import abs.api.cwi._
import abs.api.cwi.ABSFuture.done

trait PingInterface extends TypedActor {
  def start(iterations: Int): ABSFuture[Void]
  def stop: ABSFuture[Void]
  def pong: ABSFuture[Void]
}

class PingActor(pongActor: PongActor) extends PingInterface {

  var pingsLeft = 0
  var t1 = 0L

  override def start(iterations: Int): ABSFuture[Void] = messageHandler {
    t1 = System.currentTimeMillis
    pongActor.ping(this)
    pingsLeft = iterations - 1
    done
  }

  override def stop: ABSFuture[Void] = messageHandler {
    println("Done in " + (System.currentTimeMillis - t1))
    ActorSystem.shutdown()
    done
  }

  private def ping: ABSFuture[Void] = messageHandler {
    pongActor.ping(this)
    pingsLeft -= 1
    done
  }

  override def pong: ABSFuture[Void] = messageHandler {
    if (pingsLeft > 0)
      this.ping
    else {
      pongActor.stop(this)
    }
    done
  }
}
