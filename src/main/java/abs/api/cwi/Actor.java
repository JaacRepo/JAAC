package abs.api.cwi;

import java.util.concurrent.Callable;

public interface Actor {
	boolean STRICT = true;
	boolean NON_STRICT = false;
	int LOW = 0;
	int HIGH_PRIORITY = 1;

	<V> ABSFuture<V> send(Callable<ABSFuture<V>> message);
	<V> ABSFuture<V> spawn(Guard guard, Callable<ABSFuture<V>> message);
	<T,V> ABSFuture<T> getSpawn(ABSFuture<V> f, CallableGet<T, V> message, int priority, boolean strict);

	boolean sameCog(LocalActor that);

	default <T, V> ABSFuture<T> getSpawn(ABSFuture<V> f, CallableGet<T, V> message) {
		return getSpawn(f, message, LOW, NON_STRICT);
	}

	default int compare(Actor o) {
		return 0;
	}


	ABSFuture<Void> getConstructorFuture();
}
