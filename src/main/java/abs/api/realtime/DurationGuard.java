package abs.api.realtime;

import abs.api.cwi.ABSFuture;
import abs.api.cwi.Actor;
import abs.api.cwi.Guard;

public class DurationGuard extends Guard {

    int whenCalled, min, max;

    public DurationGuard(int min, int max) {
        this.whenCalled = TimedActorSystem.now();
        this.min = min;
        this.max = max;
    }

    @Override
    protected boolean evaluate() {
        return (whenCalled+min)<=TimedActorSystem.now();
    }

    @Override
    protected void addFuture(Actor a) {TimedActorSystem.addDuration(max,a); }

    @Override
    protected ABSFuture<?> getFuture() {
        return null;
    }

    @Override
    protected boolean hasFuture() {
        return false;
    }
}
