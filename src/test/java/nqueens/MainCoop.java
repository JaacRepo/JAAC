package nqueens;

import abs.api.cwi.LocalActor;

public class MainCoop extends LocalActor {

    public static void main(String[] args) {
        int numWorkers = 4;
        int priorities = 10;
        int size = 14;
        int threshold = 5;

        Master master = new Master(numWorkers, priorities, threshold, size);
        master.send(() -> master.init());
    }
}
