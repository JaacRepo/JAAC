package abs.api.cwi;

import java.util.function.Supplier;

public class PureExpressionGuard extends Guard {

	private Supplier<Boolean> expression;

	public PureExpressionGuard(Supplier<Boolean> expression) {
		super();
		this.expression = expression;
	}

	@Override
	protected boolean evaluate() {
		return expression.get();
	}

	@Override
	protected void addFuture(Actor a) { }

	@Override
	protected ABSFuture<?> getFuture() {
		return null;
	}

	@Override
	protected boolean hasFuture() {
		return false;
	}

}
