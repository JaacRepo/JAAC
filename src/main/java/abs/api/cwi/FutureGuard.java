package abs.api.cwi;

public class FutureGuard extends Guard {

	public ABSFuture<?> future;

	public FutureGuard(ABSFuture<?> future) {
		super();
		this.future = future;
	}

	@Override
	boolean evaluate() {
		return future.isDone();
	}

	@Override
	void addFuture(Actor a) {
		future.awaiting(a);
	}

	@Override
	boolean hasFuture() {
		return true;
	}
}
