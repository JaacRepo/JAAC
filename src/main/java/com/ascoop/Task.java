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
			@Override boolean hasFuture() { return false;}
			@Override void addFuture(Actor a) { }
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
			task.call().backLink(resultFuture);  // upon completion, the result is not necessarily ready
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	Future<V> getResultFuture() {
		return resultFuture;
	}
}
