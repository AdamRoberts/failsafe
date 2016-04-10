package net.jodah.recurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.jodah.recurrent.internal.util.Assert;
import net.jodah.recurrent.util.concurrent.Scheduler;
import net.jodah.recurrent.util.concurrent.Schedulers;

/**
 * Performs invocations with synchronous or asynchronous retries according to a {@link RetryPolicy}. Asynchronous
 * retries can optionally be performed on a {@link AsyncRunnable} or {@link AsyncCallable} which
 * allow invocations to be manually retried or completed.
 * 
 * @author Jonathan Halterman
 */
public class RecurrentOld {
  

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception or its resulting future is completed with an exception, the invocation
   * will be retried automatically, else if the {@code retryPolicy} has been exceeded the resulting future will be
   * completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      AsyncCallable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return future(callable, retryPolicy, Schedulers.of(executor));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception or its resulting future is completed with an exception, the invocation
   * will be retried automatically, else if the {@code retryPolicy} has been exceeded the resulting future will be
   * completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      AsyncCallable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor, AsyncListeners<T> listeners) {
    return future(callable, retryPolicy, Schedulers.of(executor), listeners);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception or its resulting future is completed with an exception, the invocation
   * will be retried automatically, else if the {@code retryPolicy} has been exceeded the resulting future will be
   * completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      AsyncCallable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      Scheduler scheduler) {
    final java.util.concurrent.CompletableFuture<T> response = new java.util.concurrent.CompletableFuture<T>();
    call(AsyncContextualCallable.ofFuture(callable), retryPolicy, scheduler, RecurrentFuture.of(response, scheduler, null), null);
    return response;
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception or its resulting future is completed with an exception, the invocation
   * will be retried automatically, else if the {@code retryPolicy} has been exceeded the resulting future will be
   * completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      AsyncCallable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      Scheduler scheduler, AsyncListeners<T> listeners) {
    Assert.notNull(listeners, "listeners");
    final java.util.concurrent.CompletableFuture<T> response = new java.util.concurrent.CompletableFuture<T>();
    call(AsyncContextualCallable.ofFuture(callable), retryPolicy, scheduler, RecurrentFuture.of(response, scheduler, listeners),
        listeners);
    return response;
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      Callable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return future(callable, retryPolicy, Schedulers.of(executor));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      Callable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor, AsyncListeners<T> listeners) {
    return future(callable, retryPolicy, Schedulers.of(executor), listeners);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      Callable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy, Scheduler scheduler) {
    final java.util.concurrent.CompletableFuture<T> response = new java.util.concurrent.CompletableFuture<T>();
    call(AsyncContextualCallable.ofFuture(callable), retryPolicy, scheduler, RecurrentFuture.of(response, scheduler, null), null);
    return response;
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> java.util.concurrent.CompletableFuture<T> future(
      Callable<java.util.concurrent.CompletableFuture<T>> callable, RetryPolicy retryPolicy, Scheduler scheduler,
      AsyncListeners<T> listeners) {
    Assert.notNull(listeners, "listeners");
    final java.util.concurrent.CompletableFuture<T> response = new java.util.concurrent.CompletableFuture<T>();
    call(AsyncContextualCallable.ofFuture(callable), retryPolicy, scheduler, RecurrentFuture.of(response, scheduler, listeners),
        listeners);
    return response;
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(AsyncCallable<T> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, Schedulers.of(executor), null, null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(AsyncCallable<T> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor, AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, Schedulers.of(executor), null,
        Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(AsyncCallable<T> callable, RetryPolicy retryPolicy,
      Scheduler scheduler) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, scheduler, null, null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code callable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code callable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(AsyncCallable<T> callable, RetryPolicy retryPolicy,
      Scheduler scheduler, AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, scheduler, null, Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code callable}, sleeping between invocation attempts according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static <T> T get(Callable<T> callable, RetryPolicy retryPolicy) {
    return call(callable, retryPolicy, null);
  }

  /**
   * Invokes the {@code callable}, sleeping between invocation attempts according to the {@code retryPolicy}, and
   * calling the {@code listeners} on recurrent events.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static <T> T get(Callable<T> callable, RetryPolicy retryPolicy, Listeners<T> listeners) {
    return call(callable, retryPolicy, Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(Callable<T> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, Schedulers.of(executor), null, null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(Callable<T> callable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor, AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, Schedulers.of(executor), null,
        Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(Callable<T> callable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, scheduler, null, null);
  }

  /**
   * Invokes the {@code callable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> get(Callable<T> callable, RetryPolicy retryPolicy, Scheduler scheduler,
      AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.of(callable), retryPolicy, scheduler, null, Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code callable}, sleeping between invocation attempts according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static <T> T get(ContextualCallable<T> callable, RetryPolicy retryPolicy) {
    return call(SyncContextualCallable.of(callable), retryPolicy, null);
  }

  /**
   * Invokes the {@code callable}, sleeping between invocation attempts according to the {@code retryPolicy}, and
   * calling the {@code listeners} on recurrent events.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static <T> T get(ContextualCallable<T> callable, RetryPolicy retryPolicy, Listeners<T> listeners) {
    return call(SyncContextualCallable.of(callable), retryPolicy, Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code runnable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static RecurrentFuture<?> run(AsyncRunnable runnable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncContextualCallable.of(runnable), retryPolicy, Schedulers.of(executor), null, null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code runnable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> run(AsyncRunnable runnable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor, AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.<T>of(runnable), retryPolicy, Schedulers.of(executor), null,
        Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code runnable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static RecurrentFuture<?> run(AsyncRunnable runnable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return call(AsyncContextualCallable.of(runnable), retryPolicy, scheduler, null, null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * Allows asynchronous invocations to manually perform retries or completion via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * <p>
   * If the {@code runnable} throws an exception, the invocation will be retried automatically, else if the
   * {@code retryPolicy} has been exceeded the resulting future will be completed exceptionally.
   * <p>
   * For non-exceptional results, retries or completion can be performed manually via the {@code runnable}'s
   * {@link AsyncInvocation} reference.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> run(AsyncRunnable runnable, RetryPolicy retryPolicy,
      Scheduler scheduler, AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.<T>of(runnable), retryPolicy, scheduler, null, Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code runnable}, sleeping between invocation attempts according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static void run(CheckedRunnable runnable, RetryPolicy retryPolicy) {
    call(Callables.of(runnable), retryPolicy, null);
  }

  /**
   * Invokes the {@code runnable}, sleeping between invocation attempts according to the {@code retryPolicy}, and
   * calling the {@code listeners} on recurrent events.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  @SuppressWarnings("unchecked")
  public static void run(CheckedRunnable runnable, RetryPolicy retryPolicy, Listeners<?> listeners) {
    call(Callables.of(runnable), retryPolicy, (Listeners<Object>) Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static RecurrentFuture<?> run(CheckedRunnable runnable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor) {
    return call(AsyncContextualCallable.of(runnable), retryPolicy, Schedulers.of(executor), null, null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code executor} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static <T> RecurrentFuture<T> run(CheckedRunnable runnable, RetryPolicy retryPolicy,
      ScheduledExecutorService executor, AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.<T>of(runnable), retryPolicy, Schedulers.of(executor), null,
        Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  public static RecurrentFuture<?> run(CheckedRunnable runnable, RetryPolicy retryPolicy, Scheduler scheduler) {
    return call(AsyncContextualCallable.of(runnable), retryPolicy, scheduler, null, null);
  }

  /**
   * Invokes the {@code runnable}, scheduling retries with the {@code scheduler} according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static <T> RecurrentFuture<T> run(CheckedRunnable runnable, RetryPolicy retryPolicy, Scheduler scheduler,
      AsyncListeners<T> listeners) {
    return call(AsyncContextualCallable.<T>of(runnable), retryPolicy, scheduler, null, Assert.notNull(listeners, "listeners"));
  }

  /**
   * Invokes the {@code runnable}, sleeping between invocation attempts according to the {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  public static void run(ContextualRunnable runnable, RetryPolicy retryPolicy) {
    call(SyncContextualCallable.of(runnable), retryPolicy, null);
  }

  /**
   * Invokes the {@code runnable}, sleeping between invocation attempts according to the {@code retryPolicy}, and
   * calling the {@code listeners} on recurrent events.
   * 
   * @throws NullPointerException if any argument is null
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  @SuppressWarnings("unchecked")
  public static void run(ContextualRunnable runnable, RetryPolicy retryPolicy, Listeners<?> listeners) {
    call(SyncContextualCallable.of(runnable), retryPolicy, (Listeners<Object>) Assert.notNull(listeners, "listeners"));
  }

  /**
   * Calls the asynchronous {@code callable} via the {@code executor}, performing retries according to the
   * {@code retryPolicy}.
   * 
   * @throws NullPointerException if any argument is null
   */
  @SuppressWarnings("unchecked")
  private static <T> RecurrentFuture<T> call(final AsyncContextualCallable<T> callable, final RetryPolicy retryPolicy,
      Scheduler scheduler, RecurrentFuture<T> future, AsyncListeners<T> listeners) {
    Assert.notNull(callable, "callable");
    Assert.notNull(retryPolicy, "retryPolicy");
    Assert.notNull(scheduler, "scheduler");

    if (future == null)
      future = new RecurrentFuture<T>(scheduler, listeners);
    AsyncInvocation invocation = new AsyncInvocation(callable, retryPolicy, scheduler, future, listeners);
    future.initialize(invocation);
    callable.initialize(invocation);
    future.setFuture((Future<T>) scheduler.schedule(callable, 0, TimeUnit.MILLISECONDS));
    return future;
  }

  /**
   * Calls the {@code callable} synchronously, performing retries according to the {@code retryPolicy}.
   * 
   * @throws RecurrentException if the {@code callable} fails with a Throwable and the retry policy is exceeded or if
   *           interrupted while waiting to perform a retry.
   */
  private static <T> T call(Callable<T> callable, RetryPolicy retryPolicy, Listeners<T> listeners) {
    Assert.notNull(callable, "callable");
    Assert.notNull(retryPolicy, "retryPolicy");

    Invocation invocation = new Invocation(retryPolicy);
    if (callable instanceof SyncContextualCallable)
      ((SyncContextualCallable<T>) callable).initialize(invocation);
    T result = null;
    Throwable failure;

    while (true) {
      try {
        failure = null;
        result = callable.call();
      } catch (Throwable t) {
        failure = t;
      }

      boolean completed = invocation.complete(result, failure, true);
      boolean success = completed && failure == null;
      boolean shouldRetry = completed ? false : invocation.canRetryForInternal(result, failure);

      // Handle failure
      if (!success && listeners != null)
        listeners.handleFailedAttempt(result, failure, invocation);

      // Handle retry needed
      if (shouldRetry) {
        try {
          Thread.sleep(TimeUnit.NANOSECONDS.toMillis(invocation.waitTime));
        } catch (InterruptedException e) {
          throw new RecurrentException(e);
        }

        if (listeners != null)
          listeners.handleRetry(result, failure, invocation);
      }

      // Handle completion
      if (completed || !shouldRetry) {
        if (listeners != null)
          listeners.complete(result, failure, invocation, success);
        if (success || failure == null)
          return result;
        RecurrentException re = failure instanceof RecurrentException ? (RecurrentException) failure
            : new RecurrentException(failure);
        throw re;
      }
    }
  }
}
