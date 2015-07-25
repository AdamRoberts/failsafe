# Recurrent
[![Build Status](https://travis-ci.org/jhalterman/recurrent.svg)](https://travis-ci.org/jhalterman/recurrent)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.jodah/recurrent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.jodah/recurrent) 

*Simple, sophisticated retries.*

## Introduction

Recurrent is a simple, zero-dependency library for performing retries. It features:

* Synchronous and asynchronous retries
* Java 6+ support with seamless Java 8 integration
* Simple integration with asynchronous APIs
* Transparent integration into public APIs

## Usage

#### Retry Policies

Recurrent supports flexible [retry policies][RetryPolicy] that allow you to express when retries should be performed.

A policy can retry on particular failures:

```java
new RetryPolicy().retryOn(ConnectException.class, SocketException.class);
new RetryPolicy().retryOn(failure -> failure instanceof ConnectException);
```

Retry on particular results:

```java
new RetryPolicy().retryWhen(null);
new RetryPolicy().retryWhen(result -> result == null);
```  

We can add a delay between retries including exponential backoff:

```java
new RetryPolicy().withDelay(1, TimeUnit.SECONDS);
new RetryPolicy().withBackoff(1, 30, TimeUnit.SECONDS);
```    

Set a max number of retries or max retry duration:

```java
new RetryPolicy()
  .withMaxRetries(100)
  .withMaxDuration(5, TimeUnit.MINUTES);
```

And of course we can combine these things into a single policy:

```java
new RetryPolicy()
  .retryOn(ConnectException.class, SocketException.class)
  .retryWhen(null)
  .withBackoff(1, 30, TimeUnit.SECONDS)
  .withMaxDuration(5, TimeUnit.MINUTES);
```

#### Synchronous Retries

Once we've defined a retry policy, we can perform retryable synchronous invocations:

```java
// Run with retries
Recurrent.run(() -> doSomething(), retryPolicy);

// Get with retries
Connection connection = Recurrent.get(() -> connect(), retryPolicy);
```

#### Asynchronous Retries

Asynchronous invocations can be performed and retried on a scheduled executor, returning a [RecurrentFuture]. When the invocation succeeds or the retry policy is exceeded, the future is completed and any [listeners](http://jodah.net/recurrent/javadoc/net/jodah/recurrent/event/package-summary.html) registered against it are called:

```java
Recurrent.get(() -> connect(), retryPolicy, executor)
  .whenSuccess(connection -> log.info("Connected to {}", connection))
  .whenFailure(failure -> log.error("Connection attempts failed", failure));
```

#### Asynchronous API Integration

Asynchronous code reports completion via indirect callbacks. Recurrent provides [ContextualRunnable] and [ContextualCallable] classes that can be used with a callback to manually perform retries or completion:

```java
Recurrent.get(invocation -> {
  someService.connect(host, port).whenComplete((result, failure) -> {
	if (failure == null)
	  invocation.complete(result);
	else if (!invocation.retryOn(failure))
      log.error("Connection attempts failed", failure);
  }
}, retryPolicy, executor));
```

#### CompletableFuture Integration

Java 8 users can use Recurrent to retry [CompletableFuture] calls:

```java
Recurrent.future(() -> CompletableFuture.supplyAsync(() -> "foo")
  .thenApplyAsync(value -> value + "bar")
  .thenAccept(System.out::println), retryPolicy, executor);
```

#### Java 8 Functional Interfaces

Recurrent can be used to create retryable Java 8 functional interfaces:

```java
Function<String, Connection> connect =
  address -> Recurrent.get(() -> connect(address), retryPolicy);
```

We can retry streams:

```java
Recurrent.run(() -> Stream.of("foo")
  .map(value -> value + "bar"), retryPolicy);
```

Individual Stream operations:

```java
Stream.of("foo")
  .map(value -> Recurrent.get(() -> value + "bar", retryPolicy));
```

Or individual CompletableFuture stages:

```java
CompletableFuture.supplyAsync(() -> Recurrent.get(() -> "foo", retryPolicy))
  .thenApplyAsync(value -> Recurrent.get(() -> value + "bar", retryPolicy));
```

#### Retry Tracking

In addition to automatically performing retries, Recurrent can also be used with APIs that have their own retry mechanism to track when a retry should be performed:

```java
RetryStats stats = new RetryStats(retryPolicy);

// On failure
if (stats.canRetryOn(someFailure))
  service.scheduleRetry(stats.getWaitTime(), TimeUnit.NANOSECONDS);
```

See the [RxJava example][RxJava] for a more details.

## Example Integrations

Recurrent was designed to integrate nicely with existing libraries. Here are some example integrations:

* [Java 8](https://github.com/jhalterman/recurrent/blob/master/src/test/java/net/jodah/recurrent/examples/Java8Example.java)
* [Netty](https://github.com/jhalterman/recurrent/blob/master/src/test/java/net/jodah/recurrent/examples/NettyExample.java)
* [RxJava]
* [Vert.x](https://github.com/jhalterman/recurrent/blob/master/src/test/java/net/jodah/recurrent/examples/VertxExample.java)

## Public API Integration

Recurrent is great for integrating into libraries and public APIs, allowing your users to configure retry policies for different opererations. One integration approach is to subclass the RetryPolicy class, then expose that as part of your API while the rest of Recurrent remains internal. Another approach is to use something like the [Maven shade plugin](https://maven.apache.org/plugins/maven-shade-plugin/) to relocate Recurrent into your project's package structure as desired.

## Docs

JavaDocs are available [here](https://jhalterman.github.com/recurrent/javadoc).

## License

Copyright 2015 Jonathan Halterman - Released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).

[RetryPolicy]: http://jodah.net/recurrent/javadoc/net/jodah/recurrent/RetryPolicy.html
[RecurrentFuture]: http://jodah.net/recurrent/javadoc/net/jodah/recurrent/RecurrentFuture.html
[ContextualRunnable]: http://jodah.net/recurrent/javadoc/net/jodah/recurrent/ContextualRunnable.html
[ContextualCallable]: http://jodah.net/recurrent/javadoc/net/jodah/recurrent/ContextualCallable.html
[CompletableFuture]: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html
[RxJava]: https://github.com/jhalterman/recurrent/blob/master/src/test/java/net/jodah/recurrent/examples/RxJavaExample.java