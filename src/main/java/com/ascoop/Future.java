package com.ascoop;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * A Future can be seen as a reference to an asynchronous computation, that may or may not result in a value returned.
 * As such, a piece of computation may delegate its completion to another async computation; this feature is implemented
 * in the {@code forward} method.
 * Thus, the main action on a future is to check the execution and completion of the asynchronous computation.
 * If there is any result (determined by the generic type {@code V}), the value can be retrieved and used using
 * continuation constructs available.
 * <p>
 * Since futures can be passed around, or forwarded (in the sense explained above), there can be multiple actors that
 * await completion of a future. This code is designed in such a way that the actors need not poll a future for its
 * completion; rather, the future retains a list of all awaiting actors and notifies them upon completion. Thus, the
 * internal implementation of an actor is simplified such that whenever an actor has no active work to do an is solely
 * awaiting completion of futures, the actor can free its thread and (so to speak) go to sleep.
 */
public class Future<V> {
    private V value = null;
    private Future<V> target = null;
    private AtomicBoolean completed = new AtomicBoolean(false);
    private Set<FutureGuard<V>> awaiting = ConcurrentHashMap.newKeySet();

    Future() {}  // not accessible to arbitrary classes

    public static <T> Future<T> done(T value) {
        return new CompletedFuture<>(value);
    }

    public static Future<Void> done() {
        return new CompletedFuture<>(null);
    }

    /**
     * This convenience method converts a collection of futures into one future, which is done when
     * all those futures are done. Then the internal done of this future is a list containing all
     * results of the input future collection. It is not possible to complete or cancel such a
     * sequenced future.
     * All input futures must contain the same type of result.
     */
    public static <R> Future<List<R>> sequence(Collection<Future<R>> futures) {
        if (futures.isEmpty())
            return done(new ArrayList<>());
        return new SequencedFuture<>(futures);
    }

    public void delegateTo(Future<V> target) {
        this.target = target;
        awaiting.forEach(fg -> fg.resetFuture(target));  // TODO empty the list? for GC?
    }

    void complete(V value) {
        if (this.completed.get()) return;
        this.value = value;
        this.completed.set(true);
        awaiting.forEach(FutureGuard::notifyDependants);
    }

    boolean isDone(FutureGuard<V> caller) {
        awaiting.add(caller);
        if (target == null)
            return this.completed.get();
        else
            return caller.resetFuture(target);
    }

    V getOrNull() {
        if (target == null)
            return this.value;
        else
            return target.getOrNull();
    }
}

class CompletedFuture<T> extends Future<T> {
    CompletedFuture(T value) {
        this.complete(value);
    }
}


class SequencedFuture<R> extends Future<List<R>> {
    SequencedFuture(Collection<Future<R>> futures) {
        AtomicInteger remaining = new AtomicInteger(futures.size());

        for (Future<R> fut : futures) {
            FutureGuard<R> awaitingGuard = new FutureGuard<R>(fut, null) {
                @Override
                void notifyDependants() {
                    if (remaining.decrementAndGet() == 0) {
                        complete(futures.stream().map(Future::getOrNull).collect(Collectors.toList()));
                    }
                }
            };
            if (fut.isDone(awaitingGuard)) {
                awaitingGuard.notifyDependants();
            }
        }
    }
}