package PingPong;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.Guard;
import abs.api.cwi.LocalActor;

public class PingA extends LocalActor {

    int pingsLeft;
    Pong pong;

    long t1;

    public PingA(int pingsLeft, Pong pong) {
        this.pingsLeft = pingsLeft;
        this.pong = pong;
    }

    ABSFuture<Void> start(){
        t1=System.currentTimeMillis();
        pong.send(()->pong.ping(this));
        pingsLeft--;
        return ABSFuture.done();
    }

    ABSFuture<Void> ping(){
        pong.send(()->pong.ping(this));
        pingsLeft--;
        return ABSFuture.done();
    }

    ABSFuture<Void> pong(){
        if(pingsLeft>0)
            this.send(()->this.ping());
        else {
            ABSFuture<Void> f= pong.send(()->pong.stop());
            spawn(Guard.convert(f),()-> {
                System.out.println("Done in "+(System.currentTimeMillis()-t1));
                pingsLeft=40000;
                this.send(()->this.start());
                return ABSFuture.done();
            });
        }
        return ABSFuture.done();
    }
}
