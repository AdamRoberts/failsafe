package net.jodah.recurrent;

import java.util.concurrent.TimeUnit;

import net.jodah.recurrent.util.Duration;

/**
 * A retryable invocation.
 * 
 * @author Jonathan Halterman
 * @param <T> result type
 */
public class Invocation {
  private RetryPolicy retryPolicy;
  private long startTime;

  /** Count of retry attempts */
  volatile int retryCount;
  /** Wait time in nanoseconds */
  volatile long waitTime;
  /** Indicates whether a retry has been requested */
  volatile boolean retryRequested;

  Invocation(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    waitTime = retryPolicy.getDelay().toNanos();
    startTime = System.nanoTime();
  }

  /**
   * Gets the number of retries that have been attempted so far.
   */
  public int getRetryCount() {
    return retryCount;
  }

  /**
   * Retries a failed invocation, returning true if the invocation's retry policy has not been exceeded, else false.
   */
  public boolean retry(Throwable failure) {
    if (retryRequested)
      return true;

    // TODO validate failure against policy
    recordFailedAttempt();
    if (!isPolicyExceeded()) {
      retryRequested = true;
      return true;
    }
    return false;
  }

  /**
   * Returns the wait time.
   */
  Duration getWaitTime() {
    return new Duration(waitTime, TimeUnit.NANOSECONDS);
  }

  /**
   * Returns true if the max retries or max duration for the retry policy have been exceeded, else false.
   */
  boolean isPolicyExceeded() {
    boolean withinMaxRetries = retryPolicy.getMaxRetries() == -1 || retryCount <= retryPolicy.getMaxRetries();
    boolean withinMaxDuration = retryPolicy.getMaxDuration() == null
        || System.nanoTime() - startTime < retryPolicy.getMaxDuration().toNanos();
    return !withinMaxRetries || !withinMaxDuration;
  }

  /**
   * Records a failed attempt and adjusts the wait time.
   */
  void recordFailedAttempt() {
    retryCount++;
    adjustForBackoffs();
    adjustForMaxDuration();
  }

  /**
   * Adjusts the wait time for backoffs.
   */
  void adjustForBackoffs() {
    if (retryPolicy.getMaxDelay() != null)
      waitTime = (long) Math.min(waitTime * retryPolicy.getDelayMultiplier(), retryPolicy.getMaxDelay().toNanos());
  }

  /**
   * Adjusts the wait time for max duration.
   */
  void adjustForMaxDuration() {
    if (retryPolicy.getMaxDuration() != null) {
      long elapsedNanos = System.nanoTime() - startTime;
      long maxRemainingWaitTime = retryPolicy.getMaxDuration().toNanos() - elapsedNanos;
      waitTime = Math.min(waitTime, maxRemainingWaitTime < 0 ? 0 : maxRemainingWaitTime);
      if (waitTime < 0)
        waitTime = 0;
    }
  }
}