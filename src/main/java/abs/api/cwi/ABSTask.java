package abs.api.cwi;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class ABSTask<V> implements Serializable, Runnable {
	static Callable<ABSFuture<Object>> emptyTask = () -> ABSFuture.done(null);

	protected Guard enablingCondition = null;
	protected final ABSFuture<V> resultFuture;
	protected Callable<ABSFuture<V>> task;

	ABSTask(Callable<ABSFuture<V>> message) {
		this(message, new Guard() {
			@Override boolean evaluate() { return true; }
			@Override boolean hasFuture() { return false;}
			@Override void addFuture(Actor a) { }
		});
	}

	ABSTask(Callable<ABSFuture<V>> message, Guard enablingCondition) {
		if (message == null)
			throw new NullPointerException();
		this.task = message;
		resultFuture = new ABSFuture<>();
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

	public ABSFuture<V> getResultFuture() {
		return resultFuture;
	}
}
