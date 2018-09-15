package com.ascoop;

public class FutureGuard extends Guard {

	public Future<?> future;

	public FutureGuard(Future<?> future) {
		super();
		this.future = future;
	}

	@Override
	boolean evaluate() {
		return future.isDone();
	}

	@Override
	void addFuture(Actor a) {
		future.awaiting(a);
	}

	@Override
	boolean hasFuture() {
		return true;
	}


}
