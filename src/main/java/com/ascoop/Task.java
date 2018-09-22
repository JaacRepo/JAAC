package com.ascoop;

import java.io.Serializable;
import java.util.concurrent.Callable;

public class Task<V> implements Serializable, Runnable {
	static Callable<Future<Object>> emptyTask = () -> Future.done(null);

	private Guard enablingCondition;
	private final Future<V> resultFuture;
	Callable<Future<V>> task;

	Task(Callable<Future<V>> message, Future<V> resultFuture) {
		this(message, resultFuture, new Guard() {
			@Override boolean evaluate() { return true; }
		});
	}

	Task(Callable<Future<V>> message, Future<V> resultFuture, Guard enablingCondition) {
		if (message == null)
			throw new NullPointerException();
		this.task = message;
		this.resultFuture = resultFuture;
		this.enablingCondition = enablingCondition;
	}

	boolean evaluateGuard() {
		return enablingCondition == null || enablingCondition.evaluate();
	}

	@Override
	public void run() {
		try {
			resultFuture.delegateTo(task.call());  // upon completion, the result is not necessarily ready
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	Future<V> getResultFuture() {
		return resultFuture;
	}
}
