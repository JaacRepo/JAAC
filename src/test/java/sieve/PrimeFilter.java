package sieve;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.LocalActor;

public class PrimeFilter extends LocalActor {

    PrimeFilter nextFilter=null;
    long [] localPrimes;
    int avalailableLocalPrimes=1;
    int id;
    public PrimeFilter(PrimeFilter nextFilter, long myPrime, int id, int numMaxLocalPrimes) {
        this.nextFilter = nextFilter;
        localPrimes = new long[numMaxLocalPrimes];
        localPrimes[0]=myPrime;
        this.id=id;
    }

    ABSFuture<Void> candidate(long n){

        return ABSFuture.done();
    }

    void handleNewPrime(long newPrime){
        if(avalailableLocalPrimes<localPrimes.length) {
            localPrimes[avalailableLocalPrimes] = newPrime;
            avalailableLocalPrimes++;
        }
        else{}
            //nextFilter = new
    }
}
