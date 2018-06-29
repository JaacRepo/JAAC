package abs.api.cwi;

public class FutureGuard extends Guard {

	public ABSFuture<?> future;

	public FutureGuard(ABSFuture<?> future) {
		super();
		this.future = future;
	}

	@Override
    protected boolean evaluate() {
		return future.isDone();
	}

	@Override
	protected void addFuture(Actor a) {
		future.awaiting(a);
	}

	@Override
	protected ABSFuture<?	> getFuture() {
		return future;
	}

	@Override
	protected boolean hasFuture() {
		return true;
	}


}
