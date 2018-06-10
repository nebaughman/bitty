package net.nyhm.bitty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * HttpService maintains a running server, restarting if the server fails.
 */
public class HttpService implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(HttpService.class);

  private final ExecutorService mExec = new RetryExecutor();

  private final ServerTask mTask;

  public HttpService(HttpServer server) {
    mTask = new ServerTask(server);
  }

  public void start() {
    log.info("Service starting");
    mExec.submit(mTask); // will retry task on exceptions
    // TODO: Server should provide a Future so service can (optionally) block until server started
    // Notice that running BittyExample tests can fail because server is being started asynchronously
    // and client calls can be made before server is bound to port and listening.
  }

  public void stop() {
    try {
      mTask.stop(); // tell any active server to stop
      mExec.shutdown(); // signal executor service to shutdown
      if (!mExec.awaitTermination(16, TimeUnit.SECONDS)) {
        mExec.shutdownNow(); // give up and halt
      }
    } catch (InterruptedException e) {
      mExec.shutdownNow(); // awaiting thread was interrupted (halt)
    }
  }

  /**
   * Calls {@link #stop()}
   */
  @Override
  public void close() throws Exception {
    stop();
  }

  private static final class ServerTask implements Callable<Void>
  {
    private final HttpServer mServer;

    ServerTask(HttpServer server) {
      mServer = server;
    }

    @Override
    public Void call() throws Exception {
      mServer.start(); // TODO: guard already running
      return null;
    }

    void stop() throws InterruptedException {
      mServer.stop(); // TODO: catch Exception|Throwable, log (rather than throw)
    }
  }

  // Adapted from https://medium.com/@aozturk/how-to-handle-uncaught-exceptions-in-java-abf819347906
  private static final class RetryExecutor extends ThreadPoolExecutor {
    RetryExecutor() {
      super(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
      super.afterExecute(r, t);

      // submit() wraps runnable in Future, must extract potential exception
      if (t == null && r instanceof Future<?>) {
        try {
          ((Future<?>)r).get();
        } catch (CancellationException e) {
          t = e;
        } catch (ExecutionException e) {
          t = e.getCause();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      // handle exception
      if (t != null) {
        log.warn("Uncaught exception", t);
        execute(r); // retry
        // TODO: RetryStrategy to decide whether to retry (Consider Guava retry)
      }
    }
  }
}
