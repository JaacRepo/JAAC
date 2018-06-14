package pingpong

import com.ascoop.ActorSystem


object MainPingPong {

  def main(args: Array[String]): Unit = {
    val N = 1000000
    val pong = new PongActor
    val ping = new PingActor(pong)
    ping.start(N)
//    Thread.sleep(1000000)
  }

}
