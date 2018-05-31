package abs.api.cwi;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static abs.api.cwi.Task.emptyTask;

class Key implements Comparable<Key> {
    private int priority;
    private int strict;

    public Key(int priority, boolean strict) {
        super();
        this.priority = priority;
        this.strict = strict ? 1 : 0;
    }

    boolean isStrict() {
        return strict == 1;
    }

    @Override
    public int compareTo(Key o) {
        // In ascending order, we should get the highest priority/strictness first
        if (o.priority == priority) {
            return o.strict - this.strict;
        } else
            return o.priority - this.priority;
    }
}

public abstract class LocalActor implements Actor {
    private Task<?> runningTask;
    private final AtomicBoolean mainTaskIsRunning = new AtomicBoolean(false);
    private ConcurrentSkipListMap<Key, ConcurrentLinkedQueue<Task<?>>> taskQueue = new ConcurrentSkipListMap<>();

    private class MainTask implements Runnable {
		@Override
		public void run() {
			if (takeOrDie()) /* {
				if (runningTask.isBlocking()) {
					BlockingExecutionContext.submit(() -> {
						runningTask.run();
						ActorSystem.submit(this);
					});
				} else */ {
					runningTask.run();
					ActorSystem.submit(this);  // instead of a loop we submit again, thus allowing other actors' tasks to get a fair chance of being scheduled in the meantime
//				}
			}
		}
	}

	private boolean takeOrDie() {
		synchronized (mainTaskIsRunning) {
			// this synchronized block is to remove the race condition between checking if nothing is there to be executed and resetting the flag mainTaskIsRunning
			for (Key key : taskQueue.keySet()) {
				ConcurrentLinkedQueue<Task<?>> bucket = taskQueue.get(key);
				for (Task<?> task : bucket) {
					if (task.evaluateGuard()) {
						runningTask = task;
						bucket.remove(task);
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

	private <V> void  schedule(Task<V> messageArgument, int priority, boolean strict) {
		if (emptyTask.equals(messageArgument.task)) {
			return;
		}

		Key key = new Key(priority, strict);
		if (taskQueue.containsKey(key)) {
			taskQueue.get(key).add(messageArgument);
		} else {
			ConcurrentLinkedQueue<Task<?>> bucket = new ConcurrentLinkedQueue<>();
			bucket.add(messageArgument);
			taskQueue.put(key, bucket);
		}
	}

	@Override
	public final <V> Future<V> send(Callable<Future<V>> message) {
		Task<V> m = new Task<>(message);
		schedule(m, LOW, NON_STRICT);
		if (notRunningThenStart()) {
			ActorSystem.submit(new MainTask());
		}
		return m.getResultFuture();
	}

	@Override
	public final <V> Future<V> spawn(Guard guard, Callable<Future<V>> message) {
		Task<V> m = new Task<>(message, guard);
		guard.addFuture(this);
		schedule(m, LOW, NON_STRICT);
		return m.getResultFuture();
	}

	// Just make the super implementation final
	@Override
	public final <T, V> Future<T> getSpawn(Future<V> f, CallableGet<T, V> message) {
		return Actor.super.getSpawn(f, message);
	}

	@Override
	public final <T, V> Future<T> getSpawn(Future<V> f, CallableGet<T, V> message, int priority, boolean strict) {
        Guard guard = Guard.convert(f);
        Task<T> m = new Task<>(() -> message.run(f.getOrNull()), guard);
        guard.addFuture(this);
        schedule(m, priority, strict);
        return m.getResultFuture();
    }

}
