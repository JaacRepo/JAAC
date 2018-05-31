package abs.api.cwi;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class Task<V> implements Serializable, Runnable {
	static Callable<Future<Object>> emptyTask = () -> Future.done(null);

	protected Guard enablingCondition = null;
	protected final Future<V> resultFuture;
	protected Callable<Future<V>> task;

	Task(Callable<Future<V>> message) {
		this(message, new Guard() {
			@Override boolean evaluate() { return true; }
			@Override boolean hasFuture() { return false;}
			@Override void addFuture(Actor a) { }
			@Override
            Future<?> getFuture() { return null;}
		});
	}

	Task(Callable<Future<V>> message, Guard enablingCondition) {
		if (message == null)
			throw new NullPointerException();
		this.task = message;
		resultFuture = new Future<>();
		this.enablingCondition = enablingCondition;
	}

	boolean evaluateGuard() {
		return enablingCondition == null || enablingCondition.evaluate();
	}

	@Override
	public void run() {
		try {
			resultFuture.forward(task.call()); // upon completion, the result is not necessarily ready
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Future<V> getResultFuture() {
		return resultFuture;
	}
}
