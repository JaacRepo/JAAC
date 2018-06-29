package examples;

public class Ping {
    int pingsLeft = 0;
    long t1 = 0L;
    Pong p;

    public Ping(int pingsLeft, Pong p) {
        this.pingsLeft = pingsLeft;
        this.p = p;
    }


    void start() {
        t1 = System.currentTimeMillis();
        p.pong(this);
        pingsLeft -= 1;

    }

    void ping() {
        if (pingsLeft > 0) {
            pingsLeft -= 1;
            p.pong(this);
        }
        else {
            p.stop();
            System.out.println("Done in " + (System.currentTimeMillis() - t1));
        }
    }
}
