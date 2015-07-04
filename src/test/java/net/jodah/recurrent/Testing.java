package net.jodah.recurrent;

import java.util.concurrent.Callable;

public class Testing {
  @FunctionalInterface
  public interface ThrowableRunnable {
    void run() throws Throwable;
  }

  public static Throwable getThrowable(ThrowableRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable t) {
      return t;
    }

    return null;
  }

  public static <T> T ignoreExceptions(Callable<T> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      return null;
    }
  }

  public static void ignoreExceptions(ThrowableRunnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
    }
  }
}
