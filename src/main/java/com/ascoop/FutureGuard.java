package com.ascoop;

import static com.ascoop.Task.emptyTask;

class FutureGuard<V> extends Guard {

	private Future<V> future;
	private final Actor awaitingActors;

	FutureGuard(Future<V> future, Actor awaitingActors) {
		super();
		this.future = future;
		this.awaitingActors = awaitingActors;
	}

	@Override
	boolean evaluate() {
		return future.isDone(this); // if future has a target, it will reset this guard's future
	}

	void notifyDependants() {
		awaitingActors.send(emptyTask);
	}

	boolean resetFuture(Future<V> newTarget) {
		this.future = newTarget;
		return future.isDone(this);
	}

	public Future<V> getFuture() {
		return future;
	}
}
