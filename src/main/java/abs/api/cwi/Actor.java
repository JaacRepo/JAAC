package abs.api.cwi;

import java.util.concurrent.Callable;

public interface Actor {
	boolean STRICT = true;
	boolean NON_STRICT = false;
	int LOW = 0;
	int HIGH = 1;

	<V> Future<V> send(Callable<Future<V>> message);
	<V> Future<V> spawn(Guard guard, Callable<Future<V>> message);
	<T,V> Future<T> getSpawn(Future<V> f, CallableGet<T, V> message, int priority, boolean strict);

	default <T, V> Future<T> getSpawn(Future<V> f, CallableGet<T, V> message) {
		return getSpawn(f, message, LOW, NON_STRICT);
	}

	default int compare(Actor o) {
		return 0;
	}
}
