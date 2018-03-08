package fibonacchi;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.LocalActor;

public class FibActor extends LocalActor {

    FibActor parent;
    int result =0;
    int respReceived=0;
    long t1;

    public FibActor(FibActor parent) {
        this.parent = parent;
    }

    public FibActor(FibActor parent, long t1) {
        this.parent = parent;
        this.t1=t1;
    }



    ABSFuture<Void> request(int n){
        if(n<=2) {
            result = 1;
            processResult(1);
        }
        else{
            FibActor f1=new FibActor(this), f2=new FibActor(this);
            f1.send(()->f1.request(n-1));
            f2.send(()->f2.request(n-2));
        }
        return ABSFuture.done();
    }

    public ABSFuture<Void> response(int n) {
        respReceived+=1;
        result+=n;
        if(respReceived==2)
            processResult(result);
        return ABSFuture.done();
    }

    void processResult(int n){
        if(parent !=null){
            parent.send(()->parent.response(n));
        }
        else{
            System.out.println("Result= "+result);
            System.out.println(System.currentTimeMillis()-t1);
        }

    }


}
