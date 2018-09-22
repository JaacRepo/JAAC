package com.ascoop;

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
}
