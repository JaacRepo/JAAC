package abs.api.cwi;

import abs.api.realtime.DurationGuard;

import java.util.function.Supplier;

public abstract class Guard {
    protected abstract boolean evaluate();

    protected abstract boolean hasFuture();

    protected abstract void addFuture(Actor a);

    protected abstract ABSFuture<?> getFuture();


    static public Guard convert(Supplier<Boolean> s) {
        return new PureExpressionGuard(s);
    }

    static public Guard convert(ABSFuture f) {
        return new FutureGuard(f);
    }

    static public Guard convert(int[] x) {
        return new DurationGuard(x[0], x[1]);
    }


    static public Guard convert(Object o) {
        if (o instanceof Supplier) {
            return convert((Supplier<Boolean>) o);
        } else if (o instanceof ABSFuture) {
            return convert((ABSFuture) o);
        } else if (o instanceof int[]) {
            int[] x = (int[]) o;
            return convert(x);
        } else if (o instanceof double[]) {
            double[] x = (double[]) o;
            int [] a =  new int[]{((int) x[0]), (int)x[1]};
            return convert(a);
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
