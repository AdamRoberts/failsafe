package net.jodah.recurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jodah.recurrent.AsyncListeners.AsyncCtxResultListener;
import net.jodah.recurrent.AsyncListeners.AsyncResultListener;
import net.jodah.recurrent.event.ContextualResultListener;
import net.jodah.recurrent.event.ContextualSuccessListener;
import net.jodah.recurrent.event.ResultListener;
import net.jodah.recurrent.event.SuccessListener;
import net.jodah.recurrent.internal.util.Assert;
import net.jodah.recurrent.internal.util.concurrent.ReentrantCircuit;

/**
 * A future result of an asynchronous operation.
 * 
 * @author Jonathan Halterman
 * @param <T> result type
 */
public class RecurrentFuture<T> implements Future<T> {
  private final ReentrantCircuit circuit = new ReentrantCircuit();
  private final Scheduler scheduler;
  private final AsyncListeners<T> listeners;
  private InvocationStats stats;
  private volatile Future<T> delegate;
  private volatile boolean done;
  private volatile boolean cancelled;
  private volatile boolean success;
  private volatile T result;
  private volatile Throwable failure;

  // Listeners
  private volatile AsyncResultListener<T> asyncCompleteListener;
  private volatile AsyncCtxResultListener<T> asyncCtxCompleteListener;
  private volatile AsyncResultListener<T> asyncFailureListener;
  private volatile AsyncCtxResultListener<T> asyncCtxFailureListener;
  private volatile AsyncResultListener<T> asyncSuccessListener;
  private volatile AsyncCtxResultListener<T> asyncCtxSuccessListener;

  RecurrentFuture(Scheduler scheduler, AsyncListeners<T> listeners) {
    this.scheduler = scheduler;
    this.listeners = listeners == null ? new AsyncListeners<T>() : listeners;
    circuit.open();
  }

  static <T> RecurrentFuture<T> of(final java.util.concurrent.CompletableFuture<T> future, Scheduler scheduler,
      AsyncListeners<T> listeners) {
    Assert.notNull(future, "future");
    Assert.notNull(scheduler, "scheduler");
    return new RecurrentFuture<T>(scheduler, listeners).whenComplete(new ResultListener<T, Throwable>() {
      @Override
      public void onResult(T result, Throwable failure) {
        if (failure == null)
          future.complete(result);
        else
          future.completeExceptionally(failure);
      }
    });
  }

  @Override
  public synchronized boolean cancel(boolean mayInterruptIfRunning) {
    boolean result = delegate.cancel(mayInterruptIfRunning);
    cancelled = true;
    circuit.close();
    return result;
  }

  @Override
  public T get() throws InterruptedException, ExecutionException {
    circuit.await();
    if (failure != null)
      throw new ExecutionException(failure);
    return result;
  }

  @Override
  public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    if (!circuit.await(timeout, Assert.notNull(unit, "unit")))
      throw new TimeoutException();
    if (failure != null)
      throw new ExecutionException(failure);
    return result;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public boolean isDone() {
    return done;
  }

  public RecurrentFuture<T> whenComplete(ContextualResultListener<? super T, ? extends Throwable> listener) {
    listeners.whenComplete(listener);
    if (done)
      listeners.handleComplete(result, failure, stats);
    return this;
  }

  public RecurrentFuture<T> whenComplete(ResultListener<? super T, ? extends Throwable> listener) {
    listeners.whenComplete(listener);
    if (done)
      listeners.handleComplete(result, failure);
    return this;
  }

  public RecurrentFuture<T> whenCompleteAsync(ContextualResultListener<? super T, ? extends Throwable> listener) {
    call(done, asyncCtxCompleteListener = new AsyncCtxResultListener<T>(listener));
    return this;
  }

  public RecurrentFuture<T> whenCompleteAsync(ContextualResultListener<? super T, ? extends Throwable> listener,
      ExecutorService executor) {
    call(done, asyncCtxCompleteListener = new AsyncCtxResultListener<T>(listener, executor));
    return this;
  }

  public RecurrentFuture<T> whenCompleteAsync(ResultListener<? super T, ? extends Throwable> listener) {
    call(done, asyncCompleteListener = new AsyncResultListener<T>(listener));
    return this;
  }

  public RecurrentFuture<T> whenCompleteAsync(ResultListener<? super T, ? extends Throwable> listener,
      ExecutorService executor) {
    call(done, asyncCompleteListener = new AsyncResultListener<T>(listener, executor));
    return this;
  }

  public RecurrentFuture<T> whenFailure(ContextualResultListener<? super T, ? extends Throwable> listener) {
    listeners.whenFailure(listener);
    if (done && !success)
      listeners.handleFailure(result, failure, stats);
    return this;
  }

  public RecurrentFuture<T> whenFailure(ResultListener<? super T, ? extends Throwable> listener) {
    listeners.whenFailure(listener);
    if (done && !success)
      listeners.handleFailure(result, failure);
    return this;
  }

  public RecurrentFuture<T> whenFailureAsync(ContextualResultListener<? super T, ? extends Throwable> listener) {
    call(done && !success, asyncCtxFailureListener = new AsyncCtxResultListener<T>(listener));
    return this;
  }

  public RecurrentFuture<T> whenFailureAsync(ContextualResultListener<? super T, ? extends Throwable> listener,
      ExecutorService executor) {
    call(done && !success, asyncCtxFailureListener = new AsyncCtxResultListener<T>(listener, executor));
    return this;
  }

  public RecurrentFuture<T> whenFailureAsync(ResultListener<? super T, ? extends Throwable> listener) {
    call(done && !success, asyncFailureListener = new AsyncResultListener<T>(listener));
    return this;
  }

  public RecurrentFuture<T> whenFailureAsync(ResultListener<? super T, ? extends Throwable> listener,
      ExecutorService executor) {
    call(done && !success, asyncFailureListener = new AsyncResultListener<T>(listener, executor));
    return this;
  }

  public RecurrentFuture<T> whenSuccess(ContextualSuccessListener<? super T> listener) {
    listeners.whenSuccess(listener);
    if (done && success)
      listeners.handleSuccess(result, stats);
    return this;
  }

  public RecurrentFuture<T> whenSuccess(SuccessListener<? super T> listener) {
    listeners.whenSuccess(listener);
    if (done && success)
      listeners.handleSuccess(result);
    return this;
  }

  public RecurrentFuture<T> whenSuccessAsync(ContextualSuccessListener<? super T> listener) {
    call(done && success,
        asyncCtxSuccessListener = new AsyncCtxResultListener<T>(Listeners.resultListenerOf(listener)));
    return this;
  }

  public RecurrentFuture<T> whenSuccessAsync(ContextualSuccessListener<? super T> listener, ExecutorService executor) {
    call(done && success,
        asyncCtxSuccessListener = new AsyncCtxResultListener<T>(Listeners.resultListenerOf(listener), executor));
    return this;
  }

  public RecurrentFuture<T> whenSuccessAsync(SuccessListener<? super T> listener) {
    call(done && success, asyncSuccessListener = new AsyncResultListener<T>(Listeners.resultListenerOf(listener)));
    return this;
  }

  public RecurrentFuture<T> whenSuccessAsync(SuccessListener<? super T> listener, ExecutorService executor) {
    call(done && success,
        asyncSuccessListener = new AsyncResultListener<T>(Listeners.resultListenerOf(listener), executor));
    return this;
  }

  synchronized void complete(T result, Throwable failure, boolean success) {
    this.result = result;
    this.failure = failure;
    this.success = success;
    done = true;
    if (success)
      AsyncListeners.call(asyncSuccessListener, asyncCtxSuccessListener, result, failure, stats, scheduler);
    else
      AsyncListeners.call(asyncFailureListener, asyncCtxFailureListener, result, failure, stats, scheduler);
    AsyncListeners.call(asyncCompleteListener, asyncCtxCompleteListener, result, failure, stats, scheduler);
    listeners.complete(result, failure, stats, success);
    circuit.close();
  }

  void initialize(InvocationStats stats) {
    this.stats = stats;
  }

  void setFuture(Future<T> delegate) {
    this.delegate = delegate;
  }

  private void call(boolean condition, AsyncCtxResultListener<T> listener) {
    if (condition)
      AsyncListeners.call(listener, result, failure, stats, scheduler);
  }

  private void call(boolean condition, AsyncResultListener<T> listener) {
    if (condition)
      AsyncListeners.call(listener, result, failure, stats, scheduler);
  }
}
