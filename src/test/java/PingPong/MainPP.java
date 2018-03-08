package PingPong;

import abs.api.cwi.LocalActor;

public class MainPP extends LocalActor {
    public static void main(String[] args) {
        int N=40000;
        Pong pong = new Pong();
        PingA ping = new PingA(N, pong);
        ping.send(()->ping.start());
    }
}
