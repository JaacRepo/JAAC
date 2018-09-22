package com.ascoop;

public class ConjunctionGuard extends Guard {
	private Guard left, right;

	public ConjunctionGuard(Guard left, Guard right) {
		super();
		this.left = left;
		this.right = right;
	}

	@Override
	boolean evaluate() {
		return left.evaluate() && right.evaluate();
	}
}
