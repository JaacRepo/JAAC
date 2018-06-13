package abs.api.cwi;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static abs.api.cwi.Task.emptyTask;

/**
 * A Future can be seen as a reference to an asynchronous computation, that may or may not result in a value returned.
 * As such, a piece of computation may delegate its completion to another async computation; this feature is implemented
 * in the {@code forward} method.
 * Thus, the main action on a future is to check the execution and completion of the asynchronous computation.
 * If there is any result (determined by the generic type {@code V}), the value can be retrieved and used using
 * continuation constructs available.
 *
 * Since futures can be passed around, or forwarded (in the sense explained above), there can be multiple actors that
 * await completion of a future. This code is designed in such a way that the actors need not poll a future for its
 * completion; rather, the future retains a list of all awaiting actors and notifies them upon completion. Thus, the
 * internal implementation of an actor is simplified such that whenever an actor has no active work to do an is solely
 * awaiting completion of futures, the actor can free its thread and (so to speak) go to sleep.
 */
public class Future<V> {
    private V value = null;
    private Future<V> dependant = null;
    private AtomicBoolean completed = new AtomicBoolean(false);
    private Set<Actor> awaitingActors = ConcurrentHashMap.newKeySet();

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

    /**
     * This method registers an actor such that it will be notified when this future is complete.
     * By first adding as awaiting and then checking my completeness, we ensure that we do not miss
     * notification, even though we may notify twice in rare cases.
     */
    void awaiting(Actor actor){
        awaitingActors.add(actor);
        if (completed.get())
            notifyDependant(actor);
    }

    /**
     * Links this instance to another future whose completion is delegated to this one.
     * By first adding as dependant and then checking my completeness, we ensure that we do not miss
     * completing it, even though we may complete it twice in rare cases.
     */
    void backLink(Future<V> linkedFuture) {
        assert dependant == null;
        dependant = linkedFuture;
        if (completed.get())
            dependant.complete(getOrNull());  // getOrNull is overridden in subclasses
    }

    void complete(V value) {
        if (this.completed.get()) return;  // double notification from delegated future
        this.value = value;
        this.completed.set(true);
        if (dependant != null) {
            dependant.complete(value);
        }
        notifyDependants();
    }

    protected void notifyDependants() {
        awaitingActors.forEach(this::notifyDependant);
    }

    private Future<Object> notifyDependant(Actor localActor) {
        return localActor.send(emptyTask);
    }

    boolean isDone() {
        return this.completed.get();
    }

    V getOrNull() {
        return this.value;
    }
}

class CompletedFuture<T> extends Future<T> {
    CompletedFuture(T value) {
        this.complete(value);
    }
}

/**
 * A sequenced future behaves like an actor in the sense that it should be notified by the futures it is awaiting.
 * This implements the Actor interface to be able to receive the wake-up message but it does not mean that
 * it is thread-safe by itself. Therefore we use an atomic boolean for completed field to ensure safety.
 */
class SequencedFuture<R> extends Future<List<R>> implements Actor {
    private final Collection<Future<R>> futures;
    private AtomicBoolean completed = new AtomicBoolean(false);

    SequencedFuture(Collection<Future<R>> futures) {
        this.futures = futures;
    }

    @Override
    void awaiting(Actor actor){
        super.awaiting(actor);
        this.futures.forEach(future -> future.awaiting(this));
        this.send(null);
    }

    @Override
    public boolean isDone() {
        return completed.get();
    }

    @Override
    public List<R> getOrNull() {
        // no need to calculate `completed` because it is always done before calling this method
        if (completed.get()) {
            return futures.stream().map(Future::getOrNull).collect(Collectors.toList());
        }
        else
            return null;
    }

    @Override
    public <V> Future<V> send(Callable<Future<V>> message) {
        completed.compareAndSet(false, futures.stream().allMatch(Future::isDone));
        if (completed.get())
            notifyDependants();
        return null;
    }

    @Override
    void complete(List<R> value) {
        throw new UnsupportedOperationException("Cannot complete a sequenced future.");
    }

    @Override
    public <V> Future<V> spawn(Guard guard, Callable<Future<V>> message) {
        return null;
    }

    @Override
    public <T, V> Future<T> getSpawn(Future<V> f, CallableGet<T, V> message, int priority, boolean strict) {
        return null;
    }
}