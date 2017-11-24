/*
 * Copyright (c) 2011-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.prometheus.impl;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.spi.metrics.EventBusMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusEventBusMetrics implements EventBusMetrics<PrometheusEventBusMetrics.Handler> {
  private static final String LOCAL = "local";
  private static final String REMOTE = "remote";

  private final Gauge handlers;
  private final Gauge pending;
  private final Counter published;
  private final Counter sent;
  private final Counter received;
  private final Counter delivered;
  private final Counter errorCount;
  private final Counter replyFailures;
  private final Histogram processTime;
  private final Histogram bytesRead;
  private final Histogram bytesWritten;

  PrometheusEventBusMetrics(MetricsStore store) {
    handlers = store.gauge("eventbus_handlers", "Number of event bus handlers in use",
      "address");
    pending = store.gauge("eventbus_pending", "Number of messages not processed yet",
      "address", "origin");
    published = store.counter("eventbus_published", "Number of messages published (publish / subscribe)",
      "address", "origin");
    sent = store.counter("eventbus_sent", "Number of messages sent (point-to-point)",
      "address", "origin");
    received = store.counter("eventbus_received", "Number of messages received",
      "address", "origin");
    delivered = store.counter("eventbus_delivered", "Number of messages delivered to handlers",
      "address", "origin");
    errorCount = store.counter("eventbus_error_count", "Number of errors",
      "address");
    replyFailures = store.counter("eventbus_reply_failures", "Number of message reply failures",
      "address");
    processTime = store.histogram("eventbus_processing_time", "Processing time",
      "address");
    bytesRead = store.histogram("eventbus_bytes_read", "Number of bytes received while reading messages from event bus cluster peers",
      "address");
    bytesWritten = store.histogram("eventbus_bytes_written", "Number of bytes sent while sending messages to event bus cluster peers",
      "address");
  }

  @Override
  public Handler handlerRegistered(String address, String repliedAddress) {
    handlers.labels(address).inc();
    return new Handler(address);
  }

  @Override
  public void handlerUnregistered(Handler handler) {
    handlers.labels(handler.address).dec();
  }

  @Override
  public void scheduleMessage(Handler handler, boolean b) {
  }

  @Override
  public void beginHandleMessage(Handler handler, boolean local) {
    pending.labels(handler.address, local ? LOCAL : REMOTE).dec();
    handler.timer = processTime.labels(handler.address).startTimer();
  }

  @Override
  public void endHandleMessage(Handler handler, Throwable failure) {
    handler.timer.observeDuration();
    if (failure != null) {
      errorCount.inc();
    }
  }

  @Override
  public void messageSent(String address, boolean publish, boolean local, boolean remote) {
    if (publish) {
      published.labels(address, local ? LOCAL : REMOTE).inc();
    } else {
      sent.labels(address, local ? LOCAL : REMOTE).inc();
    }
  }

  @Override
  public void messageReceived(String address, boolean publish, boolean local, int handlers) {
    // TODO: Business logic about pending differs from DW module. Which one is right?
    String origin = local ? LOCAL : REMOTE;
    pending.labels(address, origin).inc(handlers);
    received.labels(address, origin).inc();
    if (handlers > 0) {
      delivered.labels(address, origin).inc();
    }
  }

  @Override
  public void messageWritten(String address, int numberOfBytes) {
    bytesWritten.labels(address).observe(numberOfBytes);
  }

  @Override
  public void messageRead(String address, int numberOfBytes) {
    bytesRead.labels(address).observe(numberOfBytes);
  }

  @Override
  public void replyFailure(String address, ReplyFailure failure) {
    replyFailures.labels(address).inc();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void close() {
  }

  public static class Handler {
    private final String address;
    private Histogram.Timer timer;

    Handler(String address) {
      this.address = address;
    }
  }
}
