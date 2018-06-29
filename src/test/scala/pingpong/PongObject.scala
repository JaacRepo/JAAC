package pingpong

import abs.api.cwi.ABSFuture

class PongObject {
  var pongCount = 0

  def pong(sender: PingObject): Unit ={
    sender.ping
    pongCount += 1
  }

  def stop: Unit =  {
    println("Pong: pongs = " + pongCount);
  }
}
