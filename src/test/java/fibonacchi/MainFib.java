package fibonacchi;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.LocalActor;

public class MainFib extends LocalActor {
    public static void main(String[] args) {
        int N = 25;
        FibActor fjRunner= new FibActor(null,System.currentTimeMillis());
        ABSFuture<Void> f =fjRunner.send(()->fjRunner.request(N));

    }
}
