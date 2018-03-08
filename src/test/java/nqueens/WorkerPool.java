package nqueens;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.LocalActor;

import java.util.Set;

class Result{

}

public class WorkerPool extends LocalActor {
        Set<Worker> workers;

    public WorkerPool() { /* initialize the pool */ }
    public ABSFuture<Result> getWorker() { /* method body */ return null;}
    public ABSFuture<Void> finished(Worker w) { /* method body */ return ABSFuture.done(); }

}
