package com.ascoop;

import java.util.function.Supplier;

public class PureExpressionGuard extends Guard {

	private Supplier<Boolean> expression;

	public PureExpressionGuard(Supplier<Boolean> expression) {
		super();
		this.expression = expression;
	}

	@Override
	boolean evaluate() {
		return expression.get();
	}
}
