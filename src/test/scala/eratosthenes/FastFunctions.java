package eratosthenes;

public class FastFunctions {
    protected static boolean isLocallyPrime(
            final long candidate,
            final long[] localPrimes,
            final int startInc,
            final int endExc) {

        for (int i = startInc; i < endExc; i++) {
            final long remainder = candidate % localPrimes[i];
            if (remainder == 0) {
                return false;
            }
        }
        return true;
    }
}
