package pingpong

import abs.api.cwi._

class PingActor(pongActor: PongActor) extends SugaredActor with TypedActor[PingActor] {

  var pingsLeft = 0
  var t1 = 0L

  def this(pingsLeft: Int, pong: PongActor) {
    this(pong)
    this.pingsLeft = pingsLeft
  }

  def start: Message[Void] = messageHandler {
    t1 = System.currentTimeMillis
    pongActor ! pongActor.ping(this)
    pingsLeft -= 1
    ABSFuture.done
  }

  def ping: Message[Void] = messageHandler {
    pongActor ! pongActor.ping(this)
    pingsLeft -= 1
    ABSFuture.done
  }

  def pong: Message[Void] = messageHandler {
    if (pingsLeft > 0)
      this ! this.ping
    else {
      val f = pongActor ! pongActor.stop
      spawn(Guard.convert(f), () => {
        println("Done in " + (System.currentTimeMillis - t1))
        ActorSystem.shutdown()
        ABSFuture.done
      })
    }
    ABSFuture.done
  }
}
