package pingpong

import java.io.PrintWriter

import com.ascoop.Future._
import com.ascoop.{ActorSystem, Future, TypedActor}

trait PingInterface extends TypedActor {
  def start(iterations: Int): Future[Void]
  def pong: Future[Void]
}

class PingActor(pongActor: PongActor) extends PingInterface {

  var pingsLeft = 0
  var t1 = 0L
  val pw = new PrintWriter("ppaw.txt");
  var it =5;

  override def start(iterations: Int) = messageHandler {
    t1 = System.currentTimeMillis
    pongActor.ping(this)
    pingsLeft = iterations - 1

    //this part illusrates the use of cooperative scheduling based on an Actor's state, but for benchmarking purposes it is better
    //to use the code in pong, as (pingsLeft==0) will may be checked upon scheduling a new task in this actor, and in this PinPong
    //example we know we can verify this directly in the pong method.

    on (pingsLeft == 0) execute {
      val t2 = System.currentTimeMillis
      println(s"Finished in ${t2-t1} milliseconds")
      pongActor.stop
      it-=1
      //delete this to run only once
      if(it>0)
      this.start(1000000);
      else{
        pw.close()
        ActorSystem.shutdown()
      }
      done
    }
    //
    done
  }

  private def ping: Future[Void] = {
    pongActor.ping(this)
    pingsLeft -= 1
    done
  }

  override def pong: Future[Void] = messageHandler {
    if (pingsLeft > 0)
      this.ping

    //For benchmarking this simple example it is better to use the block of code below to run several iterations, and obtain faster results

    done
  }
}
