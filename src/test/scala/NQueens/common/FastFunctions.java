package NQueens.common;

public class FastFunctions {
    public static boolean boardValid(final int[] a, final int n) {
        int i, j;
        int p, q;

        for (i = 0; i < n; i++) {
            p = a[i];

            for (j = (i + 1); j < n; j++) {
                q = a[j];
                if (q == p || q == p - (j - i) || q == p + (j - i)) {
                    return false;
                }
            }
        }

        return true;
    }
}
