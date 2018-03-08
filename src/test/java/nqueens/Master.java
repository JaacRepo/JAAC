package nqueens;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.ActorSystem;
import abs.api.cwi.Guard;
import abs.api.cwi.LocalActor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class Master extends LocalActor {
    int numWorkers;
    int priorities;
    int threshold;
    int size;

    LinkedList<Worker> workersSeq;

    private long t1 = System.currentTimeMillis();

    public Master(int numWorkers, int priorities, int threshold, int size) {
        this.numWorkers = numWorkers;
        this.priorities = priorities;
        this.threshold = threshold;
        this.size = size;
        workersSeq = new LinkedList<>();
        for (int i = 0; i < numWorkers; i++) {
            workersSeq.add(new Worker(this, threshold, size));
        }
    }
    public ABSFuture<List<int[]>> sendWork(int[] list, int depth, int priorities) {
        //Guard nonEmpty = Guard.convert(()->!workersSeq.isEmpty());
        //return spawn(nonEmpty, ()->{
            Worker w = workersSeq.pop();
            ABSFuture<List<int[]>> outcome = w.send(() -> w.nqueensKernelPar(list, depth, priorities));
            workersSeq.add(w);
            return outcome;
        //});
    }

    public ABSFuture<Void> finished(Worker w){
        workersSeq.add(w);
        return ABSFuture.done();
    }

    public ABSFuture<Void> init() {
        t1 = System.currentTimeMillis();
        System.out.println("COOP: Boardsize =" + size);
        int[] inArray = new int[size];
        ABSFuture<List<int[]>> resultF = this.send(() -> this.sendWork(inArray, 0, priorities));
        return getSpawn(resultF, result -> {
            System.out.println("Found " + result.size() + " solutions");
            System.out.println("-------------------------------- Program successfully completed! in " + (System.currentTimeMillis() - t1));
            this.send(() -> this.init());
            return ABSFuture.done();
        });
    }
}
