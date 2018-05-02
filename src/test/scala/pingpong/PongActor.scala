package pingpong

import abs.api.cwi.{ABSFuture, ActorSystem, TypedActor}
import abs.api.cwi.ABSFuture.done

trait PongInterface extends TypedActor {
  def report: ABSFuture[Void]
  def ping(sender: PingActor): ABSFuture[Void]
}

class PongActor extends PongInterface {
  var pongCount = 0

  def ping(sender: PingActor): ABSFuture[Void] = messageHandler {
    sender.pong
    pongCount += 1
    done
  }

  def report: ABSFuture[Void] = messageHandler {
    println("Pong: pongs = " + pongCount)
    ActorSystem.shutdown()
    done
  }
}
