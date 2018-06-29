package pingpong

import com.ascoop.Future._
import com.ascoop.{Future, TypedActor}

trait PongInterface extends TypedActor {
  def stop: Future[Void]
  def ping(sender: PingActor): Future[Void]
}

class PongActor extends TypedActor {
  var pongCount = 0

   def ping(sender: PingActor): Future[Void] = messageHandler {
    sender.pong
    pongCount += 1
    done
  }

   def stop: Future[Void] = messageHandler {
    println("Pong: pongs = " + pongCount)
    //ActorSystem.shutdown()
    done
  }
}
