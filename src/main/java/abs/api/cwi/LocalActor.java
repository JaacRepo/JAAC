package abs.api.cwi;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static abs.api.cwi.ABSTask.emptyTask;

class AbsKey implements Comparable<AbsKey> {
    private int priority;
    private int strict;

    public AbsKey(int priority, boolean strict) {
        super();
        this.priority = priority;
        this.strict = strict ? 1 : 0;
    }

    boolean isStrict() {
        return strict == 1;
    }

    @Override
    public int compareTo(AbsKey o) {
        // In ascending order, we should get the highest priority/strictness first
        if (o.priority == priority) {
            return o.strict - this.strict;
        } else
            return o.priority - this.priority;
    }
}

public abstract class LocalActor implements Actor {
    private ABSTask<?> runningTask;
    private final AtomicBoolean mainTaskIsRunning = new AtomicBoolean(false);
    private ConcurrentSkipListMap<AbsKey, ConcurrentLinkedQueue<ABSTask<?>>> taskQueue = new ConcurrentSkipListMap<>();

    private ConcurrentHashMap<ABSFuture<?>, ConcurrentLinkedQueue<ABSTask<?>>> disabledQueue = new ConcurrentHashMap<>();

    private class MainTask implements Runnable {
        @Override
        public void run() {
            if (takeOrDie()) {
                runningTask.run();
                ActorSystem.submit(this);  // instead of a loop we submit again, thus allowing other actors' tasks to get a chance of being scheduled in the meantime
            }
        }
    }

    private boolean takeOrDie() {
        synchronized (mainTaskIsRunning) {
            // this synchronized block is to remove the race condition between checking if nothing is there to be executed and resetting the flag mainTaskIsRunning
            for (AbsKey key : taskQueue.keySet()) {
                ConcurrentLinkedQueue<ABSTask<?>> bucket = taskQueue.get(key);
                for (ABSTask<?> absTask : bucket) {
                    if (absTask.evaluateGuard()) {
                        runningTask = absTask;
                        bucket.remove(absTask);
                        return true;
                    }
                }
                if (!bucket.isEmpty() && key.isStrict()) {
                    // when there are disabled tasks in strict bucket, we cannot execute a lower priority task
                    mainTaskIsRunning.set(false);
                    return false;
                }
            }
            mainTaskIsRunning.set(false);
            return false;
        }
    }

    private boolean notRunningThenStart() {
        synchronized (mainTaskIsRunning) {
            return mainTaskIsRunning.compareAndSet(false, true);
        }
    }

    private <V> void schedule(ABSTask<V> messageArgument, int priority, boolean strict) {
        if (emptyTask.equals(messageArgument.task)) {
            return;
        }

        AbsKey key = new AbsKey(priority, strict);
        if (taskQueue.containsKey(key)) {
            taskQueue.get(key).add(messageArgument);
        } else {
            ConcurrentLinkedQueue<ABSTask<?>> bucket = new ConcurrentLinkedQueue<>();
            bucket.add(messageArgument);
            taskQueue.put(key, bucket);
        }
    }

    private <V> void scheduleDisabled(ABSFuture<?> disabledF, ABSTask<V> m) {
        if (disabledQueue.containsKey(disabledF))
            disabledQueue.get(disabledF).add(m);
        else {
            ConcurrentLinkedQueue<ABSTask<?>> bucket = new ConcurrentLinkedQueue<>();
            bucket.add(m);
            disabledQueue.put(disabledF, bucket);
        }
    }

    public final ABSFuture<Void> enable(ABSFuture<?> complete) {
        if (disabledQueue.containsKey(complete)) {
            for (ABSTask<?> task :
                    disabledQueue.get(complete)) {
                schedule(task, task.p, NON_STRICT);
            }
            disabledQueue.remove(complete);
        } else {
            ABSFuture<?> key = complete.getParent();
            if (disabledQueue.contains(key))
                for (ABSTask<?> task :
                        disabledQueue.get(key)) {
                    schedule(task, task.p, NON_STRICT);
                }
            disabledQueue.remove(key);
        }
        return ABSFuture.done();
    }

    @Override
    public final <V> ABSFuture<V> send(Callable<ABSFuture<V>> message) {
        ABSTask<V> m = new ABSTask<>(message, LOW);
        schedule(m, LOW, NON_STRICT);
        if (notRunningThenStart()) {
            ActorSystem.submit(new MainTask());
        }
        return m.getResultFuture();
    }

    @Override
    public final <V> ABSFuture<V> spawn(Guard guard, Callable<ABSFuture<V>> message) {
        ABSTask<V> m = new ABSTask<>(message, guard, LOW);
        guard.addFuture(this);
        if (guard.hasFuture() && !guard.evaluate()) {
            scheduleDisabled(guard.getFuture(), m);
        } else
            schedule(m, LOW, NON_STRICT);
        return m.getResultFuture();
    }

    // Just make the super implementation final
    @Override
    public final <T, V> ABSFuture<T> getSpawn(ABSFuture<V> f, CallableGet<T, V> message) {
        return Actor.super.getSpawn(f, message);
    }


	@Override
	public final <T, V> ABSFuture<T> getSpawn(ABSFuture<V> f, CallableGet<T, V> message, int priority, boolean strict) {
        Guard guard = Guard.convert(f);
        ABSTask<T> m = new ABSTask<>(() -> message.run(f.getOrNull()), guard, priority);
        guard.addFuture(this);
        if (strict || f.isDone())
            schedule(m, priority, strict);
        else
            scheduleDisabled(f, m);
        return m.getResultFuture();
    }

}
