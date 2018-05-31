package pingpong


object MainPingPong {

  def main(args: Array[String]): Unit = {
    val N = 100000000
    val pong = new PongActor
    val ping = new PingActor(pong)
    ping.start(N)
  }

}
