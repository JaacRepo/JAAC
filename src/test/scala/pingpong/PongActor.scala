package pingpong

import abs.api.cwi.{Future, ActorSystem, TypedActor}
import Future.done

trait PongInterface extends TypedActor {
  def stop: Future[Void]
  def ping(sender: PingActor): Future[Void]
}

class PongActor extends PongInterface {
  var pongCount = 0

  override def ping(sender: PingActor): Future[Void] = messageHandler {
    sender.pong
    pongCount += 1
    done
  }

  override def stop: Future[Void] = messageHandler {
    println("Pong: pongs = " + pongCount)
    //ActorSystem.shutdown()
    done
  }
}
