package abs.api.cwi;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static abs.api.cwi.ABSTask.emptyTask;

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
public class ABSFuture<V> {
    private V value = null;
    private ABSFuture<V> target = null;
    private boolean completed = false;
    private Set<Actor> awaitingActors = ConcurrentHashMap.newKeySet();

    public static <T> ABSFuture<T> done(T value) {
        return new CompletedABSFuture<>(value);
    }

    public static ABSFuture<Void> done() {
        return new CompletedVoidFuture();
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
     */
    void awaiting(Actor actor){
        this.awaiting(Collections.singleton(actor));
    }

    private void awaiting(Collection<Actor> actors){
        if (target == null) {
            awaitingActors.addAll(actors);  // this is probably not atomic. Is this OK?
            if (completed) {
                notifyDependant();
            }
        }
        // in the meantime another thread may set the target, so below code is not "else"
        if (target != null) {
            target.awaiting(actors);
            if (target.isDone()) {
                notifyDependant();
            }
        }
    }

    /**
     * Upon calling this method, the completion of this future will be delegated to the target future.
     * It means that afterwards, this future's complete method shall never be called anymore. Instead,
     * it propagates its list of waiting actors to target, who will directly notify them upon completion.
     * In case target is already completed before this method registers its own awaiting actors, it will
     * notify them directly.
     */
    void forward(ABSFuture<V> target) {
        assert this.target == null;
        this.target = target;
        // First register as dependant then check for completion.
        // This might lead to double notification in some corner cases but doesn't miss any
        target.awaiting(awaitingActors);
        if (target.isDone()) {
            notifyDependant();
        }
    }

    void complete(V value) {
        assert (!this.completed);
        assert (this.target == null);
        this.value = value;
        this.completed = true;
        notifyDependant();
    }

    protected void notifyDependant() {
        awaitingActors.forEach(localActor -> localActor.send(emptyTask));
    }

    public boolean isDone() {
        // If in the middle of running this method, a target is added such that I may actually be done, the
        // following code returns not done, but that shouldn't be a problem because the next round will be fine.
        // Though, we should make sure that there will a "next round" and that is taken care of in LocalActor takeOrDie method.
        return (target == null) ? this.completed : target.isDone();
    }

    V getOrNull() {
        return (target == null) ? this.value : target.getOrNull();
    }
}

class CompletedABSFuture<T> extends ABSFuture<T> {
    CompletedABSFuture(T value) {
        this.complete(value);
    }
}

class CompletedVoidFuture extends ABSFuture<Void> {
    @Override public boolean isDone() { return true; }
    @Override public Void getOrNull() { return null; }
}

/**
 * A sequenced future behaves like an actor in the sense that it should be notified by the futures it is awaiting.
 *
 * @param <R>
 */
class SequencedABSFuture<R> extends ABSFuture<List<R>> implements Actor {
    private final Collection<ABSFuture<R>> futures;
    private boolean completed = false;

    SequencedABSFuture(Collection<ABSFuture<R>> futures) {
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
        return completed;
    }

    @Override
    public List<R> getOrNull() {
        // no need to calculate `completed` because it is always done before calling this method
        if (completed) {
            return futures.stream().map(ABSFuture::getOrNull).collect(Collectors.toList());
        }
        else
            return null;
    }

    @Override
    public <V> ABSFuture<V> send(Callable<ABSFuture<V>> message) {
        if (!completed)
            completed = futures.stream().allMatch(ABSFuture::isDone);
        if (completed)
            notifyDependant();
        return null;
    }

    @Override
    void complete(List<R> value) {
        throw new UnsupportedOperationException("Cannot complete a sequenced future.");
    }

    @Override
    void forward(ABSFuture<List<R>> dummy) {
        throw new UnsupportedOperationException("Cannot forward a sequenced future.");
    }

    @Override
    public <V> ABSFuture<V> spawn(Guard guard, Callable<ABSFuture<V>> message) {
        return null;
    }

    @Override
    public <T, V> ABSFuture<T> getSpawn(ABSFuture<V> f, CallableGet<T, V> message, int priority, boolean strict) {
        return null;
    }
}