package net.jodah.recurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
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
  protected Scheduler scheduler;

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

  void initialize(Invocation invocation, RecurrentFuture<T> future, Scheduler scheduler) {
    this.invocation = invocation;
    this.future = future;
    this.scheduler = scheduler;
  }

  /**
   * Records an invocation result if necessary, else schedules a retry if necessary.
   */
  @SuppressWarnings("unchecked")
  void recordResult(T result, Throwable failure) {
    if (invocation.retryRequested) {
      invocation.reset();
      invocation.adjustForMaxDuration();
      scheduleRetry();
    } else if (invocation.completionRequested) {
      future.complete((T) invocation.result, invocation.failure);
      invocation.reset();
    } else {
      if (invocation.canRetryWhen(result, failure))
        scheduleRetry();
      else
        future.complete(result, failure);
    }
  }

  /**
   * Schedules a retry if the future is not done or cancelled.
   */
  @SuppressWarnings("unchecked")
  void scheduleRetry() {
    synchronized (future) {
      if (!future.isDone() && !future.isCancelled())
        future.setFuture((Future<T>) scheduler.schedule(this, invocation.waitTime, TimeUnit.NANOSECONDS));
    }
  }
}