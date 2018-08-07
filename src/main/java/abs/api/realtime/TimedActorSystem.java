package abs.api.realtime;

//import ABS.DC.ClassDeploymentComponent;
import abs.api.cwi.ABSTask;
import abs.api.cwi.Actor;
import abs.api.cwi.ActorSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TimedActorSystem extends ActorSystem {

    private static AtomicInteger symbolicTime = new AtomicInteger(0);

    private static AtomicInteger runningActors = new AtomicInteger(0);

    private static ConcurrentSkipListMap<Integer, List<Actor>> awaitingDurations = new ConcurrentSkipListMap<>();

    //private static ConcurrentLinkedQueue<ClassDeploymentComponent> deploymentComponents = new ConcurrentLinkedQueue<>();

    private TimedActorSystem() {
    }


    static public int now() {
        return symbolicTime.get();
    }

    static public void done() {
        if (runningActors.decrementAndGet() == 0) {
            //get the smallest value to advance time
            SortedSet<Integer> keys = awaitingDurations.keySet();
            //System.out.println(keys);
            int advance = keys.first();
            List<Actor> toRealease = awaitingDurations.remove(advance);
            keys.remove(advance);

            /*for (Integer k :
                    keys) {
                Integer newK = k - advance;
                List<Actor> actors = awaitingDurations.remove(k);
                awaitingDurations.put(newK, actors);
            }*/

            advanceTime(advance);

//            for (ClassDeploymentComponent dc:
//                 deploymentComponents) {
//                //dc.replenish();
//            }

            for (Actor a :
                    toRealease) {
                a.send(ABSTask.emptyTask);
            }
        }
        //System.out.println("Actor finished still running "+ runningActors.get());

    }

    static void addDuration(Integer max, Actor a){
                if(awaitingDurations.containsKey(max)){
                    awaitingDurations.get(max).add(a);
                }
                else{
                    List<Actor> al = new ArrayList<>();
                    al.add(a);
                    awaitingDurations.put(max,al);
                }
        //System.out.println(awaitingDurations);
        //System.out.println("running "+ runningActors.get());
    }

    static void advanceTime(int x) {
        symbolicTime.addAndGet(x);
    }


    static public void start(){
        runningActors.incrementAndGet();
    }

//    static void addDC(ClassDeploymentComponent dc){
//        deploymentComponents.add(dc);
//    }

}
