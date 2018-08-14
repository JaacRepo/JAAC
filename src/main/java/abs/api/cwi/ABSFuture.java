package abs.api.cwi;

import scala.math.Ordered;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static abs.api.cwi.ABSTask.emptyTask;

/**
 * A ABSFuture can be seen as a reference to an asynchronous computation, that may or may not result in a value returned.
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
public class ABSFuture<V> implements Comparable<ABSFuture<V>> {
    private V value = null;
    private ABSFuture<V> dependant = null;
    private AtomicBoolean completed = new AtomicBoolean(false);
    private Set<Actor> awaitingActors = ConcurrentHashMap.newKeySet();

    ABSFuture() {}  // not accessible to arbitrary classes

    public static <T> ABSFuture<T> done(T value) {
        return new CompletedABSFuture<>(value);
    }

    public static ABSFuture<Void> done() {
        return new CompletedABSFuture<>(null);
    }

    /**
     * This convenience method converts a collection of futures into one future, which is done when
     * all those futures are done. Then the internal done of this future is a list containing all
     * results of the input future collection. It is not possible to complete or cancel such a
     * sequenced future.
     * All input futures must contain the same type of result.
     */
    public static <R> ABSFuture<List<R>> sequence(Collection<ABSFuture<R>> futures) {
        if (futures.isEmpty())
            return done(new ArrayList<>());
        return new SequencedABSFuture<>(futures);
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
    void backLink(ABSFuture<V> linkedFuture) {
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

    private ABSFuture<Object> notifyDependant(Actor localActor) {
        return localActor.send(emptyTask);
    }

    public boolean isDone() {
        return this.completed.get();
    }

    V getOrNull() {
        return this.value;
    }

    public V getCompleted() {return getOrNull();}

    @Override
    public int compareTo(ABSFuture<V> o) {
        return 0;
    }
}

class CompletedABSFuture<T> extends ABSFuture<T> {
    CompletedABSFuture(T value) {
        this.complete(value);
    }
}

/**
 * A sequenced future behaves like an actor in the sense that it should be notified by the futures it is awaiting.
 * This implements the Actor interface to be able to receive the wake-up message but it does not mean that
 * it is thread-safe by itself. Therefore we use an atomic boolean for completed field to ensure safety.
 */
class SequencedABSFuture<R> extends ABSFuture<List<R>> implements Actor {
    private final Collection<ABSFuture<R>> futures;
    private AtomicBoolean completed = new AtomicBoolean(false);

    SequencedABSFuture(Collection<ABSFuture<R>> ABSFutures) {
        this.futures = ABSFutures;
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
            return futures.stream().map(ABSFuture::getOrNull).collect(Collectors.toList());
        }
        else
            return null;
    }

    @Override
    public <V> ABSFuture<V> send(Callable<ABSFuture<V>> message) {
        completed.compareAndSet(false, futures.stream().allMatch(ABSFuture::isDone));
        if (completed.get())
            notifyDependants();
        return null;
    }

    @Override
    void complete(List<R> value) {
        throw new UnsupportedOperationException("Cannot complete a sequenced future.");
    }

    @Override
    public <V> ABSFuture<V> spawn(Guard guard, Callable<ABSFuture<V>> message) {
        return null;
    }

    @Override
    public <T, V> ABSFuture<T> getSpawn(ABSFuture<V> f, CallableGet<T, V> message, int priority, boolean strict) {
        return null;
    }

    @Override
    public ABSFuture<Void> getConstructorFuture() {
        return null;
    }

    @Override
    public boolean sameCog(LocalActor that) {
        return false;
    }

}