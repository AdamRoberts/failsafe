package net.jodah.recurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.jodah.recurrent.event.CompletionListener;

/**
 * Performs invocations with synchronous or asynchronous retries according to a {@link RetryPolicy}. Asynchronous
 * retries can optionally be performed on a {@link ContextualRunnable} or {@link ContextualCallable} which allow
 * invocations to be manually retried or completed.
 * 
 * @author Jonathan Halterman
 */
public final class Recurrent {
  private Recurrent() {
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      Callable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return future(contextual(callable), retryPolicy, Schedulers.of(executor));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      Callable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return future(contextual(callable), retryPolicy, scheduler);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      ContextualCallable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return future(callable, retryPolicy, Schedulers.of(executor));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      ContextualCallable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      Scheduler scheduler) {
    final java.util.concurrent.CompletableFuture<T> response = new java.util.concurrent.CompletableFuture<T>();
    RecurrentFuture<T> future = new RecurrentFuture<T>(scheduler).whenComplete(new CompletionListener<T>() {
      @Override
      public void onCompletion(T result, Throwable failure) {
        if (failure == null)
          response.complete(result);
        else
          response.completeExceptionally(failure);
      }
    });

    call(AsyncCallable.ofFuture(callable), retryPolicy, scheduler, future);
    return response;
  }

  /**
   * Invokes the {@code callable}, sleeping between invocation attempts according to the {@code retryPolicy}.
   * 
   * @throws RuntimeException if the {@code callable} fails and the retry policy is exceeded or if interrupted while
   *           waiting to perform a retry. Checked exceptions, including InterruptedException, are wrapped in
   *           RuntimeException.
   */
  public static <T> T get(Callable<T> callable, RetryPolicy retryPolicy) {
    return call(callable, retryPolicy);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   */
  public static <T> RecurrentFuture<T> get(Callable<T> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncCallable.of(callable), retryPolicy, Schedulers.of(executor), null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   */
  public static <T> RecurrentFuture<T> get(Callable<T> callable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return call(AsyncCallable.of(callable), retryPolicy, scheduler, null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   */
  public static <T> RecurrentFuture<T> get(ContextualCallable<T> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncCallable.of(callable), retryPolicy, Schedulers.of(executor), null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   */
  public static <T> RecurrentFuture<T> get(ContextualCallable<T> callable, RetryPolicy retryPolicy,
      Scheduler scheduler) {
    return call(AsyncCallable.of(callable), retryPolicy, scheduler, null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   */
  public static RecurrentFuture<?> run(ContextualRunnable runnable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncCallable.of(runnable), retryPolicy, Schedulers.of(executor), null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   */
  public static RecurrentFuture<?> run(ContextualRunnable runnable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return call(AsyncCallable.of(runnable), retryPolicy, scheduler, null);
  }

  /**
   * Invokes the {@code runnable}, sleeping between invocation attempts according to the {@code retryPolicy}.
   * 
   * @throws RuntimeException if the {@code callable} fails and the retry policy is exceeded or if interrupted while
   *           waiting to perform a retry. Checked exceptions, including InterruptedException, are wrapped in
   *           RuntimeException.
   */
  public static void run(Runnable runnable, RetryPolicy retryPolicy) {
    call(Callables.of(runnable), retryPolicy);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   */
  public static RecurrentFuture<?> run(Runnable runnable, RetryPolicy retryPolicy, ScheduledExecutorService executor) {
    return call(AsyncCallable.of(runnable), retryPolicy, Schedulers.of(executor), null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   */
  public static RecurrentFuture<?> run(Runnable runnable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return call(AsyncCallable.of(runnable), retryPolicy, scheduler, null);
  }

  /**
   * Calls the {@code callable} via the {@code executor}, performing retries according to the {@code retryPolicy}.
   */
  @SuppressWarnings("unchecked")
  private static <T> RecurrentFuture<T> call(final AsyncCallable<T> callable, final RetryPolicy retryPolicy,
      Scheduler scheduler, RecurrentFuture<T> future) {
    if (future == null)
      future = new RecurrentFuture<T>(scheduler);
    callable.initialize(new Invocation(retryPolicy), future, scheduler);
    future.setFuture((Future<T>) scheduler.schedule(callable, 0, TimeUnit.MILLISECONDS));
    return future;
  }

  /**
   * Calls the {@code callable} synchronously, performing retries according to the {@code retryPolicy}.
   * 
   * @throws RuntimeException if the {@code callable} fails and the retry policy is exceeded or if interrupted while
   *           waiting to perform a retry. Checked exceptions, including InterruptedException, are wrapped in
   *           RuntimeException.
   */
  private static <T> T call(Callable<T> callable, RetryPolicy retryPolicy) {
    Invocation invocation = new Invocation(retryPolicy);
    T result = null;
    Throwable failure;

    while (true) {
      try {
        failure = null;
        result = callable.call();
      } catch (Throwable t) {
        failure = t;
      }

      if (invocation.canRetryWhen(result, failure)) {
        try {
          Thread.sleep(TimeUnit.NANOSECONDS.toMillis(invocation.waitTime));
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      } else {
        if (failure == null)
          return result;
        RuntimeException re = failure instanceof RuntimeException ? (RuntimeException) failure
            : new RuntimeException(failure);
        throw re;
      }
    }
  }

  /**
   * Returns a ContextualCallable for the {@code callable}.
   */
  private static <T> ContextualCallable<T> contextual(final Callable<T> callable) {
    return new ContextualCallable<T>() {
      @Override
      public T call(Invocation invocation) throws Exception {
        return callable.call();
      }
    };
  }
}
