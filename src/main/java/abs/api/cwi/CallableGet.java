package abs.api.cwi;

@FunctionalInterface
public interface CallableGet<T, V> {
	Future<T> run(V futValue);
}