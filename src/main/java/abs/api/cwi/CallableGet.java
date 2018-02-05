package abs.api.cwi;

@FunctionalInterface
public interface CallableGet<T, V> {
	ABSFuture<T> run(V futValue);
}