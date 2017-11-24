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
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.HttpServerMetrics;

/**
 * @author Joel Takvorian
 */
class PrometheusHttpServerMetrics {
  private final Gauge connections;
  private final Histogram bytesReceived;
  private final Histogram bytesSent;
  private final Counter errorCount;
  private final Gauge requests;
  private final Counter requestCount;
  private final Histogram processingTime;
  private final Gauge wsConnections;

  PrometheusHttpServerMetrics(MetricsStore store) {
    connections = store.gauge("http_server_connections", "Number of opened connections to the HTTP server",
      "local");
    bytesReceived = store.histogram("http_server_bytes_received", "Number of bytes received by the HTTP server",
      "local");
    bytesSent = store.histogram("http_server_bytes_sent", "Number of bytes sent by the HTTP server",
      "local");
    errorCount = store.counter("http_server_error_count", "Number of errors",
      "local");
    requests = store.gauge("http_server_requests", "Number of requests being processed",
      "local");
    requestCount = store.counter("http_server_request_count", "Number of processed requests",
      "local");
    processingTime = store.histogram("http_server_reponse_time", "Request processing time",
      "local");
    wsConnections = store.gauge("http_server_ws_connections", "Number of websockets currently opened",
      "local");
  }

  HttpServerMetrics forAddress(SocketAddress localAddress) {
    String local = Util.addressToString(localAddress);
    return new Instance(local);
  }

  class Instance implements HttpServerMetrics<Histogram.Timer, Void, Void> {
    private final String local;

    Instance(String local) {
      this.local = local;
    }

    @Override
    public Histogram.Timer requestBegin(Void socketMetric, HttpServerRequest request) {
      requests.labels(local).inc();
      return processingTime.labels(local).startTimer();
    }

    @Override
    public void requestReset(Histogram.Timer requestMetric) {
      requestCount.labels(local).inc();
      requests.labels(local).dec();
    }

    @Override
    public Histogram.Timer responsePushed(Void socketMetric, HttpMethod method, String uri, HttpServerResponse response) {
      requests.labels(local).inc();
      // TODO: not sure why (re)start timer?
      return processingTime.labels(local).startTimer();
    }

    @Override
    public void responseEnd(Histogram.Timer timer, HttpServerResponse response) {
      timer.observeDuration();
      requestCount.labels(local).inc();
      requests.labels(local).dec();
    }

    @Override
    public Void upgrade(Histogram.Timer requestMetric, ServerWebSocket serverWebSocket) {
      return null;
    }

    @Override
    public Void connected(Void socketMetric, ServerWebSocket serverWebSocket) {
      wsConnections.labels(local).inc();
      return null;
    }

    @Override
    public void disconnected(Void serverWebSocketMetric) {
      wsConnections.labels(local).dec();
    }

    @Override
    public Void connected(SocketAddress remoteAddress, String remoteName) {
      connections.labels(local).dec();
      return null;
    }

    @Override
    public void disconnected(Void socketMetric, SocketAddress remoteAddress) {
      connections.labels(local).dec();
    }

    @Override
    public void bytesRead(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
      bytesReceived.labels(local).observe(numberOfBytes);
    }

    @Override
    public void bytesWritten(Void socketMetric, SocketAddress remoteAddress, long numberOfBytes) {
      bytesSent.labels(local).observe(numberOfBytes);
    }

    @Override
    public void exceptionOccurred(Void socketMetric, SocketAddress remoteAddress, Throwable t) {
      errorCount.labels(local).inc();
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public void close() {
    }
  }
}
