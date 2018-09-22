package com.ascoop;

import static com.ascoop.Task.emptyTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class FutureGuard<V> extends Guard {

	private Future<V> future;
	private Set<Actor> awaitingActors = ConcurrentHashMap.newKeySet();

	FutureGuard(Future<V> future) {
		super();
		this.future = future;
	}

	@Override
	boolean evaluate(Actor actor) {
		awaitingActors.add(actor);
		return future.isDone(this); // if future has a target, it will reset this guard's future
	}

	void notifyDependants() {
		awaitingActors.forEach(this::notifyDependant);
	}

	private void notifyDependant(Actor localActor) {
		localActor.send(emptyTask);
	}

	boolean resetFuture(Future<V> newTarget) {
		this.future = newTarget;
		return future.isDone(this);
	}

	public Future<V> getFuture() {
		return future;
	}
}
