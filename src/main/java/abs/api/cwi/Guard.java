package abs.api.cwi;

import java.util.function.Supplier;

public abstract class Guard {
	abstract boolean evaluate();
	abstract boolean hasFuture();

	abstract void addFuture(Actor a);
	

	static public Guard convert(Object o) {
		if (o instanceof Supplier) {
			return new PureExpressionGuard((Supplier<Boolean>) o);
		} else if (o instanceof ABSFuture) {
			return new FutureGuard((ABSFuture) o);
		} else if (o instanceof Guard) {
			return (Guard) o;
		} else {
			System.out.println("Cannot make guard");
			throw new IllegalArgumentException("Cannot make a guard.");
		}
	}

	public static Guard and(Object... guards) {
		if (guards.length == 0)
			return null;
		Guard g = Guard.convert(guards[0]);
		for (int i = 1; i < guards.length; i++) {
			g = new ConjunctionGuard(g, Guard.convert(guards[i]));
		}
		return g;
	}

	public static Guard or(Object... guards) {
		if (guards.length == 0)
			return null;
		Guard g = Guard.convert(guards[0]);
		for (int i = 1; i < guards.length; i++) {
			g = new DisjunctionGuard(g, Guard.convert(guards[i]));
		}
		return g;
	}
}
