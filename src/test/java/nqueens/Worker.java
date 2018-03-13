package nqueens;

import NQueens.common.FastFunctions;
import abs.api.cwi.ABSFuture;
import abs.api.cwi.Guard;
import abs.api.cwi.LocalActor;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Worker extends LocalActor {

    Master master;
    int threshold;
    int size;

    public Worker(Master master, int threshold, int size) {
        this.master = master;
        this.threshold = threshold;
        this.size = size;
    }

    public ABSFuture<List<int[]>> nqueensKernelPar(int[] board, int depth, int priority) {
        //System.out.println("Worker received for depth " + depth);
        if (size != depth) {
            if (depth >= threshold) {
                //List<int[]> result =
                //System.out.println("Notify master");
                //master.send(() -> master.finished(this));
                return ABSFuture.done(this.nqueensKernelSeq(board, depth));
            } else {
                int newDepth = depth + 1, i = 0;
                List<ABSFuture<List<int[]>>> futures = new ArrayList<>();
                while (i < size) {
                    int[] b = new int[newDepth];
                    System.arraycopy(board, 0, b, 0, depth);
                    b[depth] = i;
                    if (FastFunctions.boardValid(b, newDepth)) {
                        ABSFuture<List<int[]>> fut = master.send(() -> master.sendWork(b, newDepth, priority - 1));
                        futures.add(fut);
                    }
                    i += 1;
                }
                return getSpawn(ABSFuture.sequence(futures), (list) -> {
                    List<int[]> result = new ArrayList<>();
                    list.forEach(result::addAll);
                    return ABSFuture.done(result);
                });
            }
        } else {
            List<int[]> r = new ArrayList<>(); // solution
            r.add(board);
            //master.send(() -> master.finished(this));
            return ABSFuture.done(r);

        }
    }

    List<int[]> nqueensKernelSeq(int[] board, int depth) {
        //System.out.println("Execute seq");
        if (size != depth) {
            List<int[]> result = new ArrayList<>();
            int[] b = new int[depth + 1];

            int i = 0;
            while (i < size) {
                System.arraycopy(board, 0, b, 0, depth);
                b[depth] = i;
                if (FastFunctions.boardValid(b, depth + 1)) {
                    result.addAll(nqueensKernelSeq(b, depth + 1));
                }
                i += 1;
            }
            return result;
        } else {
            List<int[]> r = new ArrayList<>(); // solution
            r.add(board);
            return r;
        }
    }
}
