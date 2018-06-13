
package abs.api.cwi;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ActorSystem {
	/** The main executor. */
	public static ForkJoinPool mainExecutor = new ForkJoinPool(10);

	private ActorSystem() { }

	static void submit(RecursiveAction task) {
		mainExecutor.execute(task);
	}

	public static void shutdown() {
		mainExecutor.shutdown();
	}
}
