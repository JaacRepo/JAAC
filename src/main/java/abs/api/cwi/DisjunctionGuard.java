package abs.api.cwi;

public class DisjunctionGuard extends Guard {
	private Guard left, right;

	public DisjunctionGuard(Guard left, Guard right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	protected boolean evaluate() {
		return left.evaluate() || right.evaluate();
	}

	@Override
	protected void addFuture(Actor a) {
		left.addFuture(a);
		right.addFuture(a);	
	}

	@Override
	protected ABSFuture<?> getFuture() {
		return null;
	}


	@Override
	protected boolean hasFuture() {
		return left.hasFuture()||right.hasFuture();
	}

	
}
