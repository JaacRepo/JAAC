package examples;

public class Pong {

    private int pongs =0;

    public void pong(Ping ping) {
        ping.ping();
        pongs++;

    }

    public void stop() {
        System.out.println(("Pong: pongs = " + pongs));
    }
}
