package pingpong

class PingObject (p: PongObject) {
  var pingsLeft = 0
  var t1 = 0L

  def this(pingsLeft: Int, pong: PongObject) {
    this(pong)
    this.pingsLeft = pingsLeft
  }

  def start: Unit = {
    t1 = System.currentTimeMillis
    p.pong(this)
    pingsLeft -= 1

  }

  def ping: Unit = {
    if (pingsLeft > 0) {
      pingsLeft -= 1
      p.pong(this)
    }
    else {
      p.stop
      println("Done in " + (System.currentTimeMillis - t1))
    }
  }
}
