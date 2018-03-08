package PingPong;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.LocalActor;

public class Pong extends LocalActor {

    int pongCount =0;

    public ABSFuture<Void> ping(PingA sender) {
        sender.send(()->sender.pong());
        pongCount++;
        return ABSFuture.done();
    }

    public  ABSFuture<Void> stop() {
        System.out.println("Pong: pongs = "+ pongCount);
        return ABSFuture.done();
    }
}
