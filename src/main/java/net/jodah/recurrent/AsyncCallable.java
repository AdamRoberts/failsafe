package net.jodah.recurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * An asynchronous callable with references to recurrent invocation information.
 * 
 * @author Jonathan Halterman
 * @param <T> result type
 */
abstract class AsyncCallable<T> implements Callable<T> {
  protected Invocation invocation;
  protected RecurrentFuture<T> future;
  protected ScheduledExecutorService executor;

  static <T> AsyncCallable<T> of(final Callable<T> callable) {
    return new AsyncCallable<T>() {
      @Override
      public T call() throws Exception {
        try {
          T result = callable.call();
          recordResult(result, null);
          return result;
        } catch (Exception e) {
          recordResult(null, e);
          return null;
        }
      }
    };
  }

  static <T> AsyncCallable<T> of(final ContextualCallable<T> callable) {
    return new AsyncCallable<T>() {
      @Override
      public T call() throws Exception {
        try {
          T result = callable.call(invocation);
          recordResult(result, null);
          return result;
        } catch (Exception e) {
          recordResult(null, e);
          return null;
        }
      }
    };
  }

  static AsyncCallable<?> of(final ContextualRunnable runnable) {
    return new AsyncCallable<Object>() {
      @Override
      public Void call() throws Exception {
        try {
          runnable.run(invocation);
          recordResult(null, null);
        } catch (Exception e) {
          recordResult(null, e);
        }

        return null;
      }
    };
  }

  static AsyncCallable<?> of(final Runnable runnable) {
    return new AsyncCallable<Object>() {
      @Override
      public Void call() throws Exception {
        try {
          runnable.run();
          recordResult(null, null);
        } catch (Exception e) {
          recordResult(null, e);
        }

        return null;
      }
    };
  }

  static <T> AsyncCallable<T> ofFuture(final ContextualCallable<java.util.concurrent.CompletableFuture<T>> callable) {
    return new AsyncCallable<T>() {
      @Override
      public synchronized T call() throws Exception {
        try {
          callable.call(invocation).whenComplete(new BiConsumer<T, Throwable>() {
            @Override
            public void accept(T innerResult, Throwable failure) {
              recordResult(innerResult, failure);
            }
          });
        } catch (Exception e) {
          recordResult(null, e);
        }

        return null;
      }
    };
  }

  void initialize(Invocation invocation, RecurrentFuture<T> future, ScheduledExecutorService executor) {
    this.invocation = invocation;
    this.future = future;
    this.executor = executor;
  }

  /**
   * Records an invocation result if necessary, else scheduling a retry if necessary.
   */
  @SuppressWarnings("unchecked")
  void recordResult(T result, Throwable failure) {
    if (invocation.retryRequested) {
      // Handle manually requested retry
      invocation.resetUserState();
      invocation.adjustForMaxDuration();
      scheduleRetry();
    } else if (invocation.completionRequested) {
      // Handle manually requested completion
      future.complete((T) invocation.result, invocation.failure);
      invocation.resetUserState();
    } else if (failure != null) {
      // Handle failed invocation
      invocation.recordFailedAttempt();
      if (invocation.retryPolicy.allowsRetriesFor(failure) && !invocation.isPolicyExceeded())
        scheduleRetry();
      else
        future.complete(null, failure);
    } else {
      // Handle successful invocation
      future.complete(result, null);
    }
  }

  /**
   * Schedules a retry if the future is not done or cancelled.
   */
  void scheduleRetry() {
    synchronized (future) {
      if (!future.isDone() && !future.isCancelled())
        future.setFuture(executor.schedule(this, invocation.waitTime, TimeUnit.NANOSECONDS));
    }
  }
}