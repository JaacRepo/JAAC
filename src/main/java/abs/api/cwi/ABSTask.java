package abs.api.cwi;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class ABSTask<V> implements Serializable, Runnable {
	public static Callable<ABSFuture<Object>> emptyTask = () -> ABSFuture.done(null);

	protected Guard enablingCondition = null;
	protected final ABSFuture<V> resultFuture;
	protected Callable<ABSFuture<V>> task;

	ABSTask(Callable<ABSFuture<V>> message) {
		this(message, new Guard() {
			@Override
			protected boolean evaluate() { return true; }
			@Override
			protected boolean hasFuture() { return false;}
			@Override
			protected void addFuture(Actor a) { }
			@Override
			protected ABSFuture<?> getFuture() { return null; }
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
			task.call().backLink(resultFuture);  // upon completion, the result is not necessarily ready
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public ABSFuture<V> getResultFuture() {
		return resultFuture;
	}
}
