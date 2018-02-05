package abs.api.cwi;

public class DisjunctionGuard extends Guard {
	private Guard left, right;

	public DisjunctionGuard(Guard left, Guard right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	boolean evaluate() {
		return left.evaluate() || right.evaluate();
	}

	@Override
	void addFuture(Actor a) {
		left.addFuture(a);
		right.addFuture(a);	
	}
	
	
	

	@Override
	boolean hasFuture() {
		return left.hasFuture()||right.hasFuture();
	}

	
}
