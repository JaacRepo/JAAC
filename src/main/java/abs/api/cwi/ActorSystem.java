
package abs.api.cwi;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.RecursiveAction;

public class ActorSystem {
	/** The main executor. */
	protected static ForkJoinPool mainExecutor = new ForkJoinPool(10, NonDaemonForkJoinWorkerThread::new, null, false);

	protected ActorSystem() { }

	public static void submit(RecursiveAction task) {
		mainExecutor.execute(task);
	}

	public static void shutdown() {
		mainExecutor.shutdown();
	}

	private static class NonDaemonForkJoinWorkerThread extends ForkJoinWorkerThread {
        NonDaemonForkJoinWorkerThread(ForkJoinPool pool) {
            super(pool);
            this.setDaemon(false);
        }
    }
}
