package pingpong

import abs.api.cwi.{ABSFuture, SugaredActor, TypedActor}

class PongActor extends SugaredActor with TypedActor[PongActor] {
  var pongCount = 0

  def ping(sender: PingActor): Message[Void] = messageHandler{
    sender! sender.pong
    pongCount += 1
    ABSFuture.done
  }

  def stop: Message[Void] = messageHandler {
    println("Pong: pongs = " + pongCount)
    ABSFuture.done
  }
}
