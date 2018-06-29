/*
 * The class that will be extended by ABS classes
 * 
 */
package abs.api.cwi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActorSystem {
	/** The main executor. */
	protected static ExecutorService mainExecutor = Executors.newFixedThreadPool(10);

	protected ActorSystem() { }

	static void submit(Runnable task) {
		mainExecutor.submit(task);
	}

	public static void shutdown() {
		mainExecutor.shutdown();
	}
}
