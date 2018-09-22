package com.ascoop;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.ascoop.Task.emptyTask;

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

    private class MainTask extends RecursiveAction {
		@Override
		public void compute() {
			if (takeOrDie()) {
					runningTask.run();
					ActorSystem.submit(new MainTask());  // instead of a loop we submit again, thus allowing other actors' tasks to get a fair chance of being scheduled in the meantime
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

	/**
	 * Having this check synchronized with takeOrDie is necessary for avoiding race conditions like the following:
	 * When MainTask is about to stop because it found no enabled messages while at the same time a new message
	 * is being sent who would then need to keep the MainTask running. Without synchronization it could happen that
	 * the new message thinks the MainTask is still running and will thus pick up a message, while it is about to die.
	 * Having this synchronization will then let the MainTask first die and then reactivated again by the new message.
	 * Note that the new message could also be the special emptyTask that notifies the actor that a future is enabled now.
	 */
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
		Task<V> m = new Task<>(message, new Future<>());
		schedule(m, LOW, NON_STRICT);
		if (notRunningThenStart()) {
			ActorSystem.submit(new MainTask());
		}
		return m.getResultFuture();
	}

	@Override
	public final <V> Future<V> spawn(Guard guard, Callable<Future<V>> message) {
		Task<V> m = new Task<>(message, new Future<>(), guard);
		schedule(m, LOW, NON_STRICT);
		return m.getResultFuture();
	}

	@Override
	public final <T, V> Future<T> getSpawn(Future<V> input, CallableGet<T, V> message, int priority, boolean strict) {
        return getSpawn(new Future<>(), input, message, priority, strict);
    }

	protected final <T, V> Future<T> getSpawn(Future<T> output, Future<V> input, CallableGet<T, V> message, int priority, boolean strict) {
		FutureGuard<V> guard = new FutureGuard<>(input, this);
		Task<T> m = new Task<>(() -> message.run(guard.getFuture().getOrNull()), output, guard);
		schedule(m, priority, strict);
		return output;
	}
}
